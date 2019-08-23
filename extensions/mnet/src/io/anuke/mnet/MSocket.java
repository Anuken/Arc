package io.anuke.mnet;

import io.anuke.arc.collection.*;
import io.anuke.arc.util.pooling.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class MSocket{

    /**
     * Size of receiving packet queue. If it overflows, data might be lost.
     */
    public static int receivingQueueSize = 5000;
    //Очередь с принятными отсортированными сообщениями.
    //byte[] - Пакет, String - Сообщение дисконнекта, Float - пинг.
    final AtomicQueue<Object> queue = new AtomicQueue<Object>(receivingQueueSize);
    final InetAddress address;
    final int port;
    //Main useful stuff
    private final UDPSocket udp;
    private final AtomicInteger seq = new AtomicInteger();
    //Отправленные надёжные пакеты, требущие подтверждения или пересылаются.
    private final SortedIntList<ResendPacket> requestList = new SortedIntList<>();
    //Очередь в которой полученные пакеты сортируются, если они были получены в неправильной последовательности
    //byte[] - пакет или пинг если длинна == 0, byte[][] - batch пакет
    private final SortedIntList<Object> receivingSortQueue = new SortedIntList<Object>();
    //Аккумулирует bigRequest пока они не соберутся полностью.
    private final HashMap<Integer, BigStorage> bigAccumulator = new HashMap<Integer, BigStorage>();
    volatile long lastTimeReceivedMsg;
    //Params
    volatile long resendCD = 125;
    long pingCD = 2500;
    int inactivityTimeout = 15000;
    int bufferSize = 512;
    private volatile SocketState state;
    private boolean isClientSocket;
    private volatile int lastInsertedSeq = -1;
    private MSerializer serializer;
    private Pool<ResendPacket> sendPacketPool = new Pool<ResendPacket>(){
        protected ResendPacket newObject(){
            return new ResendPacket();
        }
    };
    //Осуществляет контроль над частотой ресендов
    private volatile CongestionManager cm = new DefaultCongestionManager();
    private int bigSeqCounter = 1;
    //datagrams
    private DatagramPacket sendPacket;
    private DatagramPacket ackPacket;
    private DatagramPacket receivePacket;
    private DatagramPacket pingResponsePacket;
    private DatagramPacket serverConnectionResponse; //Ответ сервера на connectionRequest.
    private byte[] sendBuffer;
    private byte[] receiveBuffer;
    private byte[] ackBuffer;
    private byte[] pingResponseBuffer;

    //processing
    private boolean processing = false; //true if processing right now
    private boolean interrupted = false; //true if interrupted processing.

    //Utils
    private MServerSocket server;
    private ArrayList<PingListener> pingListeners = new ArrayList<>();
    private ArrayList<DCListener> dcListeners = new ArrayList<>();
    private Object userData = null;
    private float currentPing;
    private long lastPingSendTime;

    public MSocket(InetAddress address, int port, MSerializer serializer) throws SocketException{
        this(address, port, 1024, 7000, 2500, 100, serializer);
    }

    public MSocket(InetAddress address, int port, int bufferSize, int inactivityTimeout, int pingFrequency, int resendFrequency, MSerializer serializer) throws SocketException{
        this(new JavaUDPSocket(), address, port, bufferSize, inactivityTimeout, pingFrequency, resendFrequency, serializer);
    }

    public MSocket(UDPSocket udp, InetAddress address, int port, int bufferSize, int inactivityTimeout, int pingFrequency, int resendFrequency, MSerializer serializer) throws SocketException{
        this.state = SocketState.NOT_CONNECTED;
        this.serializer = serializer;
        this.udp = udp;
        this.address = address;
        this.port = port;
        this.isClientSocket = true;
        this.bufferSize = bufferSize;
        this.pingCD = pingFrequency;
        this.resendCD = resendFrequency;
        this.inactivityTimeout = inactivityTimeout;
        this.serverConnectionResponse = null;
        initPackets(bufferSize, address, port);
    }

    /**
     * ONLY USED BY SERVERS_SOCKET to construct new SUBSOCKET
     */
    MSocket(UDPSocket udp, InetAddress address, int port, int bufferSize){
        this.state = SocketState.NOT_CONNECTED;
        this.udp = udp;
        this.address = address;
        this.port = port;
        this.isClientSocket = false;
        this.bufferSize = bufferSize;
    }

    /**
     * Initialization done by server after Socket was accepted
     */
    void init(MServerSocket serverSocket, byte[] fullResponseData, int inactivityTimeout, int pingFrequency, int resendFrequency, MSerializer serializer){
        this.serializer = serializer;
        this.state = SocketState.CONNECTED;
        this.server = serverSocket;
        this.lastTimeReceivedMsg = System.currentTimeMillis();
        this.lastPingSendTime = System.currentTimeMillis();
        this.pingCD = pingFrequency;
        this.resendCD = resendFrequency;
        this.inactivityTimeout = inactivityTimeout;

        this.serverConnectionResponse = new DatagramPacket(fullResponseData, fullResponseData.length);
        this.serverConnectionResponse.setAddress(address);
        this.serverConnectionResponse.setPort(port);
        initPackets(bufferSize, address, port);
    }

    private void initPackets(int bufferSize, InetAddress address, int port){
        this.sendBuffer = new byte[bufferSize];
        this.ackBuffer = new byte[bufferSize];
        this.receiveBuffer = new byte[bufferSize];
        this.pingResponseBuffer = new byte[13];
        this.pingResponseBuffer[0] = PacketType.pingResponse;

        this.sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length);
        this.sendPacket.setAddress(address);
        this.sendPacket.setPort(port);

        this.ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
        this.ackPacket.setAddress(address);
        this.ackPacket.setPort(port);

        this.receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        this.pingResponsePacket = new DatagramPacket(pingResponseBuffer, pingResponseBuffer.length);
        this.pingResponsePacket.setAddress(address);
        this.pingResponsePacket.setPort(port);
    }

    public void connectAsync(final Object request, final int timeout, final ServerResponseHandler handler){
        new Thread(() -> {
            try{
                ServerResponse connect = connect(request, timeout);
                handler.handle(connect);
            }catch(IOException e){
                e.printStackTrace();
                handler.handle(new ServerResponse(ResponseType.WRONG_STATE, null));
            }
        }).start();
    }

    public ServerResponse connect(Object data, int timeout) throws IOException{
        byte[] req = serializer.serialize(data);
        return connect(req, timeout);
    }

    private ServerResponse connect(byte[] data, int timeout) throws IOException{
        if(!isClientSocket || state != SocketState.NOT_CONNECTED)
            return new ServerResponse(ResponseType.WRONG_STATE);
        state = SocketState.CONNECTING;

        udp.setReceiveTimeout(timeout);
        udp.connect(address, port);
        sendPacket.setAddress(address);
        sendPacket.setPort(port);
        int attempts;
        int wait;
        if(timeout < 600){
            timeout = 1000;
        }

        if(timeout < 2000){
            wait = 333;
            attempts = timeout / wait;
        }else{
            wait = 500;
            attempts = timeout / wait;
        }

        //Make request
        sendBuffer[0] = PacketType.connectionRequest;
        System.arraycopy(data, 0, sendBuffer, 1, data.length);
        long startTime = System.currentTimeMillis();

        //send until all attempts are over or we get response.
        udp.setReceiveTimeout(wait);
        while(attempts > 0){
            try{
                udp.send(sendPacket);
                udp.receive(receivePacket);
            }catch(IOException e){
                if(udp.isClosed()){
                    state = SocketState.CLOSED;
                    return new ServerResponse(ResponseType.WRONG_STATE);
                }else if(e instanceof PortUnreachableException){
                    return new ServerResponse(ResponseType.NO_RESPONSE, null);
                }else{
                    if(attempts-- == 0){
                        state = SocketState.NOT_CONNECTED;
                        return new ServerResponse(ResponseType.NO_RESPONSE);
                    }
                }
            }
            byte pType = receiveBuffer[0];
            if(pType == PacketType.connectionResponseOk || pType == PacketType.connectionResponseError || System.currentTimeMillis() - startTime > timeout){
                break;
            }
        }

        byte packetType = receiveBuffer[0];

        if(packetType == PacketType.connectionResponseError){
            state = SocketState.NOT_CONNECTED;
            return new ServerResponse(ResponseType.REJECTED);
        }
        if(packetType != PacketType.connectionResponseOk){
            state = SocketState.NOT_CONNECTED;
            return new ServerResponse(ResponseType.NO_RESPONSE);
        }

        int len = receivePacket.getLength();
        Object responseData;
        if(len == 5){
            responseData = null;
        }else{
            try{
                responseData = serializer.deserialize(receiveBuffer, 5, len - 5);
            }catch(Exception e){
                e.printStackTrace();
                responseData = null;
            }
        }
        udp.setReceiveTimeout(inactivityTimeout);
        state = SocketState.CONNECTED;
        this.lastTimeReceivedMsg = System.currentTimeMillis();
        lastPingSendTime = lastTimeReceivedMsg;
        currentPing = (lastTimeReceivedMsg - startTime);
        launchReceiveThread();
        return new ServerResponse(ResponseType.ACCEPTED, responseData);
    }

    //***********//
    //* ACTIONS *//
    //***********//

    public void sendUnreliable(Object o){
        if(isConnected()){
            sendBuffer[0] = PacketType.unreliable;
            int size = serializer.serialize(o, sendBuffer, 1);
            sendPacket.setLength(size + 1);
            try{
                udp.send(sendPacket);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void send(Object o){
        if(isConnected()){
            byte[] fullPackage = serializer.serialize(o, 5);
            int seq = this.seq.getAndIncrement();
            fullPackage[0] = PacketType.reliableRequest;
            PacketType.putInt(fullPackage, seq, 1);
            saveRequest(seq, fullPackage);
            sendData(fullPackage);
        }
    }

    public void sendBig(Object o){
        byte[] big = serializer.serialize(o);
        int id = bigSeqCounter++;
        if(big.length < bufferSize - 5){
            sendSerialized(big);
        }else{
            if(isConnected()){
                int maxPerPacket = bufferSize - 9; //503

                int packs = big.length / maxPerPacket;
                if(big.length % (maxPerPacket) > 0){
                    packs++;
                }

                int offset = 0;
                for(int i = 0; i < packs; i++){
                    int currentSize = Math.min(maxPerPacket, big.length - offset);
                    byte[] singlePacket = new byte[currentSize + 9];
                    singlePacket[0] = PacketType.bigRequest;
                    int seq = this.seq.getAndIncrement();
                    PacketType.putInt(singlePacket, seq, 1);
                    PacketType.putShort(singlePacket, id, 5);
                    PacketType.putShort(singlePacket, packs, 7);
                    System.arraycopy(big, offset, singlePacket, 9, currentSize);

                    saveRequest(seq, singlePacket);
                    sendData(singlePacket);
                    offset += currentSize;
                }
            }
        }
    }

    public void send(NetBatch batch){
        if(isConnected()){
            int size = batch.size();
            switch(size){
                case 0:
                    return;
                case 1:
                    send(batch.get(0));
                    return;
            }

            int bufferSize = this.bufferSize;

            ByteBatch bb = batch.convertAndGet(serializer);
            int i = 0;
            while(i < size){
                int seq = this.seq.getAndIncrement();
                Object[] tuple = PacketType.buildSafeBatch(seq, PacketType.batch, bb, i, bufferSize);
                byte[] fullPackage = (byte[])tuple[0];
                i = ((Integer)tuple[1]);
                saveRequest(seq, fullPackage);
                sendData(fullPackage);
            }
        }
    }

    public void sendSerialized(byte[] data){
        if(isConnected()){
            int seq = this.seq.getAndIncrement();
            byte[] fullPackage = PacketType.build5byte(PacketType.reliableRequest, seq, data);
            saveRequest(seq, fullPackage);
            sendData(fullPackage);
        }
    }

    public void sendSerUnrel(byte[] data){
        if(isConnected()){
            sendBuffer[0] = PacketType.unreliable;
            System.arraycopy(data, 0, sendBuffer, 1, data.length);
            sendPacket.setLength(data.length + 1);
            try{
                udp.send(sendPacket);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void update(SocketProcessor processor){
        if(isConnected()){
            processData(processor);
            checkResendAndPing();
        }
    }

    //***********//
    //* GET-SET *//
    //***********//
    public boolean isClosed(){
        return state == SocketState.CLOSED;
    }

    public boolean close(){
        return close(DCType.CLOSED);
    }

    public boolean close(String msg){
        SocketState state = this.state;

        if(state != SocketState.CONNECTING && state != SocketState.CONNECTED) return false;
        this.state = SocketState.CLOSED;

        sendData(PacketType.build5byte(PacketType.disconnect, 0, MUtils.trimDCMessage(msg, bufferSize)));

        if(isClientSocket){
            udp.close();
        }else{
            server.removeMe(this);
        }

        notifyDcListenersAndRemoveAll(msg);
        return true;
    }

    public UDPSocket getUdp(){
        return udp;
    }

    public int getLocalPort(){
        return udp.getLocalPort();
    }

    public SocketState getState(){
        return state;
    }

    public void stop(){
        interrupted = true;
    }

    public boolean isProcessing(){
        return processing;
    }

    public void addPingListener(PingListener pl){
        pingListeners.add(pl);
    }

    public void removePingListener(PingListener pl){
        pingListeners.remove(pl);
    }

    public void addDcListener(DCListener dl){
        dcListeners.add(dl);
    }

    public void removeDcListener(DCListener dl){
        dcListeners.remove(dl);
    }

    public void removeAllListeners(){
        pingListeners.clear();
        dcListeners.clear();
    }

    public boolean isConnected(){
        return state == SocketState.CONNECTED;
    }

    public float getPing(){
        return currentPing;
    }

    public <T> T getUserData(){
        return (T)userData;
    }

    public <T> T setUserData(Object userData){
        T oldData = (T)this.userData;
        this.userData = userData;
        return oldData;
    }

    public void setPingFrequency(int millis){
        this.pingCD = millis;
    }

    public CongestionManager getCongestionManager(){
        return cm;
    }

    public void setCongestionManager(CongestionManager cm){
        this.cm = cm;
    }

    public long getResendDelay(){
        return resendCD;
    }

    public void setResendDelay(long delay){
        resendCD = delay;
    }

    public int getBufferSize(){
        return bufferSize;
    }

    public InetAddress getRemoteAddress(){
        return address;
    }

    public int getRemotePort(){
        return port;
    }

    public MSerializer getSerializer(){
        return serializer;
    }

    //********//
    //* CODE *//
    //********//

    private void deserializeAndPut(byte[] data, int offset, int length){
        Object obj;
        try{
            obj = serializer.deserialize(data, offset, length);
        }catch(Exception e){
            e.printStackTrace();
            return;
        }
        if(obj != null)
            queue.put(obj);
    }

    private void launchReceiveThread(){
        new Thread(() -> {
            byte[] receiveBuffer = MSocket.this.receiveBuffer;
            DatagramPacket packet = MSocket.this.receivePacket;
            UDPSocket udp = MSocket.this.udp;
            boolean keepRunning = true;
            while(keepRunning){
                try{
                    udp.receive(packet);
                }catch(IOException e){
                    if(udp.isClosed()){
                        return;
                    }else{
                        continue;
                    }
                }

                int length = packet.getLength();
                if(length > 5){
                    byte type = receiveBuffer[0];
                    receiveData(receiveBuffer, type, length);
                }

            }
        }).start();
    }

    /**
     * @param fullPacket Весь пакет. Длинна == bufferSize.
     * @param type Первый бит
     * @param length Длина информативной части пакета. > 5
     */
    void receiveData(byte[] fullPacket, byte type, int length){
        lastTimeReceivedMsg = System.currentTimeMillis();
        final int seq = PacketType.extractInt(fullPacket, 1);
        switch(type){
            case PacketType.reliableRequest:
                sendAck(seq);
                int expectedSeq1 = lastInsertedSeq + 1;
                if(seq == expectedSeq1){
                    lastInsertedSeq = seq;
                    deserializeAndPut(fullPacket, 5, length - 5);
                    updateReceiveOrderQueue();
                }else if(seq > expectedSeq1){
                    Object obj;
                    try{
                        obj = serializer.deserialize(fullPacket, 5, length - 5);
                    }catch(Exception e){
                        e.printStackTrace();
                        break;
                    }
                    if(obj != null)
                        addToWaitings(seq, obj);
                }
                break;
            case PacketType.reliableAck:
                removeFromWaitingForAck(seq, lastTimeReceivedMsg);
                break;
            case PacketType.unreliable:
                deserializeAndPut(fullPacket, 1, length - 1);
                break;
            case PacketType.batch:
                sendAck(seq);
                int expectedSeq2 = lastInsertedSeq + 1;
                if(seq < expectedSeq2){
                    break;
                }

                try{
                    Object[] batchPackets;
                    if(seq == expectedSeq2){
                        lastInsertedSeq = seq;
                        batchPackets = PacketType.breakBatchDown(fullPacket, serializer);
                        for(Object batchPacket : batchPackets){
                            queue.put(batchPacket);
                        }
                        updateReceiveOrderQueue();
                    }else{
                        batchPackets = PacketType.breakBatchDown(fullPacket, serializer);
                        addToWaitings(seq, batchPackets);
                    }
                }catch(Exception ignore){
                }
                break;
            case PacketType.bigRequest:
                sendAck(seq);
                int expectedSeqBig = lastInsertedSeq + 1;
                if(seq >= expectedSeqBig){
                    toBigAccumulator(seq, fullPacket);
                }
                break;
            case PacketType.pingRequest:
                final long startTime = PacketType.extractLong(fullPacket, 5);
                sendPingResponse(seq, startTime);

                int expectSeq3 = this.lastInsertedSeq + 1;

                if(expectSeq3 == seq){
                    lastInsertedSeq = seq;
                    updateReceiveOrderQueue();
                }else if(seq > expectSeq3){
                    addToWaitings(seq, new PingPacket(0));
                }
                break;
            case PacketType.pingResponse:
                final long startingTime = PacketType.extractLong(fullPacket, 5);
                boolean removed = removeFromWaitingForAck(seq, lastTimeReceivedMsg);
                if(removed){
                    PingPacket ping = new PingPacket(((float)(System.nanoTime() - startingTime)) / 1000000f);
                    queue.put(ping);
                }
                break;
            case PacketType.connectionRequest:
                if(!isClientSocket){
                    try{
                        udp.send(serverConnectionResponse);
                    }catch(IOException ignore){
                        ignore.printStackTrace();
                    }
                }
                break;
            case PacketType.connectionResponseOk:
            case PacketType.connectionResponseError:
                //Ничего.
                break;
            case PacketType.disconnect:
                String msg = new String(fullPacket, 5, length - 5);
                queue.put(new DisconnectionPacket(DisconnectionPacket.EVENT_RECEIVED, msg));
                break;
            default:
                System.err.println("Unknown message type: " + type);
        }
    }

    private void toBigAccumulator(int seq, byte[] fullPacketBig){
        byte[] userData = new byte[fullPacketBig.length - 9];
        System.arraycopy(fullPacketBig, 9, userData, 0, userData.length);
        int id = PacketType.extractShort(fullPacketBig, 5);
        BigStorage bs = bigAccumulator.get(id);
        if(bs == null){
            bs = new BigStorage(PacketType.extractShort(fullPacketBig, 7));
            bigAccumulator.put(id, bs);
        }

        //Если заполнили BigStorage
        if(bs.put(seq, userData)){
            int lastSeqOfParts = lastInsertedSeq;
            int totalByteSize = 0;
            for(SortedIntList.Node<byte[]> part : bs.parts){
                totalByteSize += part.value.length;
                lastSeqOfParts = part.index;
            }
            byte[] result = new byte[totalByteSize];
            int offset = 0;
            for(SortedIntList.Node<byte[]> part : bs.parts){
                byte[] value = part.value;

                System.arraycopy(value, 0, result, offset, value.length);
                offset += value.length;
            }
            bigAccumulator.remove(id);

            Object deserialized = null;
            try{
                deserialized = serializer.deserialize(result);
            }catch(Exception e){
                e.printStackTrace();
            }
            //Собрали массив целиком. И из него объект, удалив BigStorage.

            int expectedSeq = lastInsertedSeq + 1;
            int firstPartSeq = bs.parts.first.index;

            if(firstPartSeq == expectedSeq){ //Если мы прямо сейчас ожидаем этот объект, то просто присваиваем lastInsertedSeq последним seq части и заносим объект в очередь
                lastInsertedSeq = lastSeqOfParts;
                try{
                    queue.put(deserialized);
                }catch(Exception e){
                    e.printStackTrace();
                }

                updateReceiveOrderQueue();
            }else if(firstPartSeq > expectedSeq){ //Если же мы впереди всё ещё ждём чего-то, то добавляем объект в Waitings. Заполняя остатки seq PingPacket-ами
                PingPacket pp = new PingPacket(0);
                addToWaitings(firstPartSeq, deserialized != null ? deserialized : pp);
                for(int i = 1; i < bs.parts.size; i++){
                    addToWaitings(firstPartSeq + i, pp);
                }
            }
        }
    }

    /**
     * Отправляем ответ на пинг запрос. Тупо пакет с type, seq, startTime
     */
    private void sendPingResponse(int seq, long startTime){
        PacketType.putInt(pingResponseBuffer, seq, 1);
        PacketType.putLong(pingResponseBuffer, startTime, 5);
        try{
            udp.send(pingResponsePacket);
        }catch(IOException e){
        }
    }


    /**
     * Добавить в очередь на зачисление в queue.
     * @param seq номер
     * @param userData Object - пакет, Object[] - Батч пакет, PingPacket - пинг.
     */
    private void addToWaitings(int seq, Object userData){
        receivingSortQueue.insert(seq, userData);
    }

    private boolean removeFromWaitingForAck(int seq, long currentTime){
        synchronized(requestList){
            ResendPacket removed = requestList.remove(seq);
            if(removed != null){
                sendPacketPool.free(removed);
                resendCD = cm.calculateDelay(removed, currentTime, resendCD);
                return true;
            }
        }
        return false;
    }

    private void processData(SocketProcessor processor){
        if(processing){
            throw new ConcurrentModificationException("Can't be processed by 2 threads at the same time");
        }
        if(!isConnected()) return;

        processing = true;
        interrupted = false;
        try{

            Object poll = queue.poll();
            while(poll != null){

                if(poll instanceof PingPacket){
                    currentPing = ((PingPacket)poll).newPing;
                    for(PingListener pingListener : pingListeners){
                        pingListener.pingChanged(this, currentPing);
                    }
                }else if(poll instanceof DisconnectionPacket){
                    interrupted = true;
                    state = SocketState.CLOSED;
                    DisconnectionPacket dcPacket = (DisconnectionPacket)poll;

                    if(dcPacket.type == DisconnectionPacket.TIMED_OUT){
                        byte[] bytes = PacketType.build5byte(PacketType.disconnect, 0, MUtils.trimDCMessage(dcPacket.message, bufferSize));
                        sendData(bytes);
                    }

                    if(isClientSocket){
                        udp.close();
                    }else{
                        server.removeMe(this);
                    }

                    notifyDcListenersAndRemoveAll(dcPacket.message);
                    break;
                }else{
                    processor.process(this, poll);
                }
                if(interrupted){
                    break;
                }
                poll = queue.poll();
            }

        }catch(Throwable t){
            t.printStackTrace();
        }
        processing = false;
        return;
    }

    void sendAck(int seq){
        ackBuffer[0] = PacketType.reliableAck;
        PacketType.putInt(ackBuffer, seq, 1);
        try{
            udp.send(ackPacket);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    void notifyDcListenersAndRemoveAll(String msg){
        for(DCListener dcListener : dcListeners){
            dcListener.socketClosed(this, msg);
        }
        dcListeners.clear();
        pingListeners.clear();
    }

    void checkResendAndPing(){
        final long currTime = System.currentTimeMillis();
        if(currTime - lastPingSendTime > pingCD){
            sendPing();
            lastPingSendTime = currTime;
        }
        long resendCD = this.resendCD;
        synchronized(requestList){
            for(SortedIntList.Node<ResendPacket> pack : requestList){
                ResendPacket packet = pack.value;
                if(currTime - packet.sendTime > resendCD){
                    sendData(packet.data);
                    packet.sendTime = currTime;
                    packet.resends++;
                }
            }
        }

        if(currTime - lastTimeReceivedMsg > inactivityTimeout){
            queue.put(new DisconnectionPacket(DisconnectionPacket.TIMED_OUT, DCType.TIME_OUT));
        }
    }

    private void saveRequest(int seq, byte[] fullPackage){
        synchronized(requestList){
            requestList.insert(seq, sendPacketPool.obtain().set(fullPackage));
        }
    }

    /**
     * Проверить объекты которые ждут свою очередь
     */
    private void updateReceiveOrderQueue(){
        int expectedSeq;

        SortedIntList<Object> queue = this.receivingSortQueue;
        expectedSeq = lastInsertedSeq + 1;
        Object mayBeData = queue.remove(expectedSeq);

        while(mayBeData != null){
            this.lastInsertedSeq = expectedSeq;
            if(mayBeData instanceof PingPacket){

            }else if(mayBeData instanceof Object[]){
                for(Object packet : ((Object[])mayBeData)){
                    this.queue.put(packet);
                }
            }else{
                this.queue.put(mayBeData);
            }
            expectedSeq = lastInsertedSeq + 1;
            mayBeData = queue.remove(expectedSeq);
        }
    }

    void sendData(byte[] fullPackage){
        System.arraycopy(fullPackage, 0, sendBuffer, 0, fullPackage.length);
        try{
            sendPacket.setLength(fullPackage.length);
            udp.send(sendPacket);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void sendPing(){
        int seq = this.seq.getAndIncrement();
        byte[] fullPackage = new byte[13];
        fullPackage[0] = PacketType.pingRequest;
        PacketType.putInt(fullPackage, seq, 1);
        PacketType.putLong(fullPackage, System.nanoTime(), 5);
        saveRequest(seq, fullPackage);
        sendData(fullPackage);
    }

    public String toString(){
        return "{" +
        "state=" + state +
        ", ping=" + currentPing +
        ", address=" + address +
        ", port=" + port +
        ", processing=" + processing +
        ", userData=" + userData +
        '}';
    }

    static class DisconnectionPacket{

        public static final int SELF_CLOSED = 1;
        public static final int EVENT_RECEIVED = 2;
        public static final int TIMED_OUT = 3;

        public int type;
        public String message;

        public DisconnectionPacket(int type, String message){
            this.type = type;
            this.message = message;
        }
    }

    private static class PingPacket{
        float newPing;

        public PingPacket(float newPing){
            this.newPing = newPing;
        }
    }

    public class ResendPacket{
        public long sendTime;
        public int resends;
        public byte[] data;

        public ResendPacket set(byte[] data){
            this.sendTime = System.currentTimeMillis();
            this.data = data;
            this.resends = 0;
            return this;
        }
    }
}
