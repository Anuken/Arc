package arc.discord;

import arc.discord.IPCClient.Packet.*;
import arc.discord.IPCClient.Pipe.*;
import arc.func.*;
import arc.util.*;
import arc.util.serialization.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public final class IPCClient{
    private static int pid;
    private static long clientId;
    private static volatile Pipe pipe;
    private static Thread readThread = null;

    public static Cons<String> onActivityJoin = secret -> {};
    public static Cons<String> onActivitySpectate = secret -> {};
    public static Cons2<String, User> onActivityJoinRequest = (secret, user) -> {};
    public static Runnable onReady = () -> {};
    public static Cons<Throwable> onDisconnected = error -> {};
    public static Cons<Jval> onClose = json -> {};

    /**
     * Opens the connection between the IPCClient and Discord.<p>
     *
     * <b>This must be called before any data is exchanged between the
     * IPCClient and Discord.</b>
     * @throws IllegalStateException There is an open connection on this IPCClient.
     */
    public static void connect(long clientId) throws Exception{
        IPCClient.clientId = clientId;
        String version = System.getProperty("java.version");

        int major;
        if(version.contains(".")){
            major = Strings.parseInt(version.substring(0, version.indexOf('.')));
        }else{
            major = Strings.parseInt(version);
        }

        //this is only supported on Java >= 16, since that's where Unix sockets were added
        if(major < 16){
            throw new Exception("Discord RPC is not supported on < Java 16. Your version: " + version);
        }

        //use reflection to call Java 9 API
        Class<?> c = Class.forName("java.lang.ProcessHandle");
        Object current = c.getMethod("current").invoke(null);
        pid = ((Long)c.getMethod("pid").invoke(current)).intValue();

        checkConnected(false);
        pipe = Pipe.openPipe(clientId);

        onReady.run();
        startReading();
    }

    /**
     * Sends a {@link RichPresence} to the Discord client.<p>
     * <p>
     * This is where the IPCClient will officially display
     * a Rich Presence in the Discord client.<p>
     * <p>
     * Sending this again will overwrite the last provided
     * {@link RichPresence}.
     * @param presence The {@link RichPresence} to send.
     * @throws IllegalStateException If a connection was not made prior to invoking
     * this method.
     * @see RichPresence
     */
    public static void send(RichPresence presence){
        checkConnected(true);
        pipe.send(OpCode.frame,
        Jval.newObject()
        .put("cmd", "SET_ACTIVITY")
        .put("args", Jval.newObject()
        .put("pid", pid)
        .put("activity", presence == null ? null : presence.toJson())));
    }

    /**
     * Adds an event {@link Event} to this IPCClient.<br>
     * If the provided {@link Event} is added more than once,
     * it does nothing.
     * Once added, there is no way to remove the subscription
     * other than {@link #close() closing} the connection
     * and creating a new one.
     * @param sub The event {@link Event} to add.
     * @throws IllegalStateException If a connection was not made prior to invoking
     * this method.
     */
    public static void subscribe(Event sub){
        checkConnected(true);
        if(!sub.subscribable)
            throw new IllegalStateException("Cannot subscribe to " + sub + " event!");
        pipe.send(OpCode.frame,
        Jval.newObject()
        .put("cmd", "SUBSCRIBE")
        .put("evt", sub.value));
    }

    public static PipeStatus getStatus(){
        return pipe == null ? PipeStatus.unitialized : pipe.status;
    }

    /**
     * Attempts to close an open connection to Discord.<br>
     * @throws IllegalStateException If a connection was not made prior to invoking
     * this method.
     */
    public static void close(){
        checkConnected(true);

        try{
            pipe.close();
        }catch(IOException ignored){
        }
    }

    /**
     * Makes sure that the client is connected (or not) depending on if it should
     * for the current state.
     * @param connected Whether to check in the context of the IPCClient being
     * connected or not.
     */
    private static void checkConnected(boolean connected){
        if(connected && getStatus() != Pipe.PipeStatus.connected)
            throw new IllegalStateException(String.format("IPCClient (ID: %d) is not connected!", clientId));
        if(!connected && getStatus() == Pipe.PipeStatus.connected)
            throw new IllegalStateException(String.format("IPCClient (ID: %d) is already connected!", clientId));
    }

    /**
     * Initializes this IPCClient's {@link IPCClient#readThread readThread}
     * and calls the first {@link Pipe#read()}.
     */
    private static void startReading(){
        readThread = new Thread(() -> {
            try{
                Packet p;
                while((p = pipe.read()).op != OpCode.close){
                    Jval json = p.data;
                    if(json.has("cmd") && json.getString("cmd").equals("DISPATCH")){
                        try{
                            Jval data = json.get("data");
                            switch(json.getString("evt")){
                                case "ACTIVITY_JOIN":
                                    onActivityJoin.get(data.getString("secret"));
                                    break;
                                case "ACTIVITY_SPECTATE":
                                    onActivitySpectate.get(data.getString("secret"));
                                    break;
                                case "ACTIVITY_JOIN_REQUEST":
                                    Jval u = data.get("user");
                                    onActivityJoinRequest.get(data.getString("secret", null),
                                    new User(u.getString("username"), u.getString("discriminator"),
                                    Long.parseLong(u.getString("id")), u.getString("avatar", null))
                                    );
                                    break;
                            }
                        }catch(Exception e){
                            Log.err(e);
                        }
                    }
                }
                pipe.status = PipeStatus.disconnected;
                onClose.get(p.data);
            }catch(Throwable ex){
                pipe.status = PipeStatus.disconnected;
                onDisconnected.get(ex);
            }
        });

        readThread.start();
    }

    public enum Event{
        activityJoin("ACTIVITY_JOIN"),
        activitySpectate("ACTIVITY_SPECTATE"),
        activityJoinRequest("ACTIVITY_JOIN_REQUEST");

        public final String value;

        Event(String value){
            this.value = value;
        }
    }

    public static class Packet{
        public final OpCode op;
        public final Jval data;

        public Packet(OpCode op, Jval data){
            this.op = op;
            this.data = data;
        }

        public byte[] toBytes(){
            byte[] d = data.toString().getBytes();
            ByteBuffer packet = ByteBuffer.allocate(d.length + 2 * Integer.BYTES);
            packet.putInt(Integer.reverseBytes(op.ordinal()));
            packet.putInt(Integer.reverseBytes(d.length));
            packet.put(d);
            return packet.array();
        }

        @Override
        public String toString(){
            return "Pkt:" + op + data.toString();
        }

        public enum OpCode{
            handshake, frame, close, ping, pong
        }
    }

    public abstract static class Pipe{
        private static final int version = 1;
        // a list of system property keys to get IPC file from different unix systems.
        private final static String[] unixPaths = {"XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP"};

        public PipeStatus status = PipeStatus.connecting;

        public static Pipe openPipe(long clientId) throws Exception{

            for(int i = 0; i < 10; i++){
                try{
                    String location = getPipeLocation(i);
                    Pipe pipe;
                    if(OS.isWindows){
                        pipe = new WindowsPipe(location);
                    }else if(OS.isLinux || OS.isMac){
                        pipe = new UnixPipe(location);
                    }else{
                        throw new RuntimeException("Unsupported OS: " + OS.name);
                    }
                    pipe.send(OpCode.handshake, Jval.newObject().put("v", version).put("client_id", Long.toString(clientId)));
                    pipe.status = PipeStatus.connected;

                    //TODO log data
                    Packet p = pipe.read();
                    Log.debug(p.data);

                    return pipe;
                }catch(IOException ignored){
                }
            }

            throw new Exception("No Discord client found.");
        }

        /**
         * Finds the IPC location in the current system.
         * @param i Index to try getting the IPC at.
         * @return The IPC location.
         */
        private static String getPipeLocation(int i){
            if(System.getProperty("os.name").contains("Win"))
                return "\\\\?\\pipe\\discord-ipc-" + i;
            String tmppath = null;
            for(String str : unixPaths){
                tmppath = System.getenv(str);
                if(tmppath != null)
                    break;
            }
            if(tmppath == null)
                tmppath = "/tmp";
            return tmppath + "/discord-ipc-" + i;
        }

        /**
         * Sends json with the given {@link OpCode}.
         * @param op The {@link OpCode} to send data with.
         * @param data The data to send.
         */
        public void send(OpCode op, Jval data){
            try{
                write(new Packet(op, data.put("nonce", UUID.randomUUID().toString())).toBytes());
            }catch(IOException ex){
                status = PipeStatus.disconnected;
            }
        }

        /**
         * Blocks until reading a {@link Packet} or until the
         * read thread encounters bad data.
         * @return A valid {@link Packet}.
         */
        public abstract Packet read() throws Exception;

        public abstract void write(byte[] b) throws IOException;

        public abstract void close() throws IOException;

        public enum PipeStatus{
            unitialized, connecting, connected, closed, disconnected
        }
    }

    public static class RichPresence{
        public String state;
        public String details;
        public long startTimestamp;
        public long endTimestamp;
        public String largeImageKey;
        public String largeImageText;
        public String smallImageKey;
        public String smallImageText;
        public String partyId;
        public int partySize;
        public int partyMax;
        public String matchSecret;
        public String joinSecret;
        public String spectateSecret;
        public boolean instance;

        public Jval toJson(){
            return Jval.newObject()
            .put("state", state)
            .put("details", details)
            .put("timestamps", Jval.newObject()
            .put("start", startTimestamp)
            .put("end", endTimestamp)
            .put("assets", Jval.newObject())
            .put("large_image", largeImageKey)
            .put("large_text", largeImageText)
            .put("small_image", smallImageKey)
            .put("small_text", smallImageText))
            .put("party", partyId == null ? null : Jval.newObject()
            .put("id", partyId)
            .put("size", Jval.newArray().add(partySize).add(partyMax)))
            .put("secrets", Jval.newObject()
            .put("join", joinSecret)
            .put("spectate", spectateSecret)
            .put("match", matchSecret))
            .put("instance", instance);
        }
    }

    public static class UnixPipe extends Pipe{
        private final SocketChannel socket;
        private final ByteBuffer buffer = ByteBuffer.allocate(1024 * 32);

        UnixPipe(String location) throws Exception{
            //this is java 16+ API, so needs reflection
            Method method = SocketChannel.class.getMethod("open", SocketAddress.class);
            Class<?> addressc = Class.forName("java.net.UnixDomainSocketAddress");
            Method construct = addressc.getMethod("of", String.class);

            socket = (SocketChannel)method.invoke(null, construct.invoke(null, location));
            socket.configureBlocking(true);
        }

        @Override
        public Packet read() throws Exception{
            buffer.position(0);

            while(socket.read(buffer) <= 0 && status == PipeStatus.connected){
                try{
                    Thread.sleep(100);
                }catch(InterruptedException ignored){
                }
            }

            buffer.position(0);

            if(status == PipeStatus.disconnected) throw new IOException("Disconnected!");
            if(status == PipeStatus.closed) return new Packet(OpCode.close, null);

            OpCode op = OpCode.values()[Integer.reverseBytes(buffer.getInt())];
            byte[] data = new byte[Integer.reverseBytes(buffer.getInt())];
            buffer.get(data);

            Packet p = new Packet(op, Jval.read(data));
            return p;
        }

        @Override
        public void write(byte[] b) throws IOException{
            socket.write(ByteBuffer.wrap(b));
        }

        @Override
        public void close() throws IOException{
            send(OpCode.close, Jval.newObject());
            status = PipeStatus.closed;
            socket.close();
        }
    }

    public static class User{
        public final String name;
        public final String discriminator;
        public final long id;
        @Nullable
        public final String avatar;

        public User(String name, String discriminator, long id, String avatar){
            this.name = name;
            this.discriminator = discriminator;
            this.id = id;
            this.avatar = avatar;
        }
    }

    public static class WindowsPipe extends Pipe{
        private final RandomAccessFile file;

        WindowsPipe(String location){
            try{
                this.file = new RandomAccessFile(location, "rw");
            }catch(FileNotFoundException e){
                throw new RuntimeException(e);
            }
        }

        @Override
        public void write(byte[] b) throws IOException{
            file.write(b);
        }

        @Override
        public Packet read() throws Exception{
            while(file.length() == 0 && status == PipeStatus.connected){
                try{
                    Thread.sleep(100);
                }catch(InterruptedException ignored){
                }
            }

            if(status == PipeStatus.disconnected)
                throw new IOException("Disconnected!");

            if(status == PipeStatus.closed)
                return new Packet(OpCode.close, null);

            OpCode op = OpCode.values()[Integer.reverseBytes(file.readInt())];
            int len = Integer.reverseBytes(file.readInt());
            byte[] d = new byte[len];

            file.readFully(d);
            Packet p = new Packet(op, Jval.read(d));
            return p;
        }

        @Override
        public void close() throws IOException{
            send(OpCode.close, Jval.newObject());
            status = PipeStatus.closed;
            file.close();
        }

    }
}
