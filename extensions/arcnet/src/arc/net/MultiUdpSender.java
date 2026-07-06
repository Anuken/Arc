package arc.net;

import java.io.*;
import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import arc.util.*;
import arc.struct.*;

/**
 * Linux only.
 * @author BalaM314
 */
public class MultiUdpSender implements AutoCloseable {

	public static final int maxSimultaneousAddresses = 50;

	public static final short AF_INET = 2;

	public static final Linker linker = Linker.nativeLinker();
	public static final SymbolLookup stdlib = linker.defaultLookup();
	public static final StructLayout captureStateLayout = Linker.Option.captureStateLayout();
	public static final VarHandle errno = captureStateLayout.varHandle(PathElement.groupElement("errno"));
	public static final MethodHandle sendmmsg;

	public static final StructLayout sockaddr = MemoryLayout.structLayout(
		ValueLayout.JAVA_SHORT.withName("family"),
		ValueLayout.JAVA_SHORT.withName("port").withOrder(ByteOrder.BIG_ENDIAN),
		ValueLayout.JAVA_INT.withName("address").withOrder(ByteOrder.BIG_ENDIAN),
		MemoryLayout.paddingLayout(8)
	);
	public static final MemoryLayout sockaddrArray = MemoryLayout.sequenceLayout(maxSimultaneousAddresses, sockaddr);
	public static final VarHandle sockaddr_family = sockaddrArray.varHandle(PathElement.sequenceElement(), PathElement.groupElement("family"));
	public static final VarHandle sockaddr_port = sockaddrArray.varHandle(PathElement.sequenceElement(), PathElement.groupElement("port"));
	public static final VarHandle sockaddr_address = sockaddrArray.varHandle(PathElement.sequenceElement(), PathElement.groupElement("address"));

	public static final StructLayout iovec = MemoryLayout.structLayout(
		ValueLayout.ADDRESS.withName("ptr"),
		ValueLayout.JAVA_LONG.withName("len") //size_t
	);
	public static final VarHandle iovec_ptr = iovec.varHandle(PathElement.groupElement("ptr"));
	public static final VarHandle iovec_len = iovec.varHandle(PathElement.groupElement("len"));

	public static final StructLayout mmsghdr = MemoryLayout.structLayout(
		ValueLayout.ADDRESS.withName("dest_addr"),
		ValueLayout.JAVA_INT.withName("dest_addr_len"), //socklen_t
		MemoryLayout.paddingLayout(4),
		ValueLayout.ADDRESS.withName("iovecs"),
		ValueLayout.JAVA_LONG.withName("iovecs_len"), //size_t
		ValueLayout.ADDRESS.withName("control"),
		ValueLayout.JAVA_LONG.withName("control_len"), //size_t
		ValueLayout.JAVA_INT.withName("flags"),
		MemoryLayout.paddingLayout(4), //end of msghdr struct
		ValueLayout.JAVA_INT.withName("msg_len"),
		MemoryLayout.paddingLayout(4)
	);
	public static final MemoryLayout mmsghdrArray = MemoryLayout.sequenceLayout(maxSimultaneousAddresses, mmsghdr);
	public static final VarHandle mmsghdr_dest_addr = mmsghdrArray.varHandle(PathElement.sequenceElement(), PathElement.groupElement("dest_addr"));
	public static final VarHandle mmsghdr_dest_addr_len = mmsghdrArray.varHandle(PathElement.sequenceElement(), PathElement.groupElement("dest_addr_len"));
	public static final VarHandle mmsghdr_iovecs = mmsghdrArray.varHandle(PathElement.sequenceElement(), PathElement.groupElement("iovecs"));
	public static final VarHandle mmsghdr_iovecs_len = mmsghdrArray.varHandle(PathElement.sequenceElement(), PathElement.groupElement("iovecs_len"));
	public static final VarHandle mmsghdr_control = mmsghdrArray.varHandle(PathElement.sequenceElement(), PathElement.groupElement("control"));
	public static final VarHandle mmsghdr_control_len = mmsghdrArray.varHandle(PathElement.sequenceElement(), PathElement.groupElement("control_len"));
	public static final VarHandle mmsghdr_flags = mmsghdrArray.varHandle(PathElement.sequenceElement(), PathElement.groupElement("flags"));
	public static final VarHandle mmsghdr_msg_len = mmsghdrArray.varHandle(PathElement.sequenceElement(), PathElement.groupElement("msg_len"));

	private DatagramChannel channel;
	/** File descriptor for channel's associated UDP socket. */
	private int fdi;
	/** Number of addresses this sender is connected to. */
	private int numConnectedAddresses = 0;
	private Arena arena = Arena.ofConfined();
	private MemorySegment dest_addr_array = arena.allocate(sockaddrArray);
	private MemorySegment dest_addr = arena.allocate(sockaddr);
	private MemorySegment data = arena.allocate(iovec);
	private MemorySegment msgvec = arena.allocate(mmsghdrArray);
	private MemorySegment captureState = arena.allocate(captureStateLayout);

	static {
		var loc = stdlib.find("sendmmsg");
		if(loc.isPresent()){
			sendmmsg = linker.downcallHandle(
				loc.get(),
				//int sendmmsg(int sockfd, struct mmsghdr msgvec[n], unsigned int n, int flags);
				FunctionDescriptor.of(
					ValueLayout.JAVA_INT,
					ValueLayout.JAVA_INT,
					ValueLayout.ADDRESS,
					ValueLayout.JAVA_INT,
					ValueLayout.JAVA_INT
				),
				Linker.Option.captureCallState("errno")
				//The java address sanitizer complains when we set this field to point at heap-allocated bytebuffer contents
				//It's complaining because it thinks the GC could mess with the data while we're accessing it
				//This is safe because the method is marked as critical, so the GC will not run while the method is running
				//and the method immediately copies the data out into the kernel buffer before returning
				//TODO: Does this have to be marked as critical to block GC while this is running?
				//We only need to make sure the ByteBuffer doesn't get dealloced
				//The ByteBuffer is still in scope when the method returns -BalaM314
				// Linker.Option.critical(true)
			);
		} else {
			sendmmsg = null;
		}
	}

	public MultiUdpSender(){
		if(sendmmsg == null){
			throw new RuntimeException("Native method not available, please use the default implementation.");
		}

		for(long i = 0; i < maxSimultaneousAddresses; i ++){
			//Construct an array of mmsghdr structs
			//Each struct points to the same data (an [iovec; 1])
			//but a different socket address
			sockaddr_family.set(dest_addr_array, 0L, i, AF_INET);
			mmsghdr_dest_addr.set(msgvec, 0L, i, dest_addr_array.asSlice(
				sockaddrArray.byteOffset(PathElement.sequenceElement(i)), sockaddr.byteSize()));
			mmsghdr_dest_addr_len.set(msgvec, 0L, i, (int)sockaddr.byteSize());
			mmsghdr_iovecs.set(msgvec, 0L, i, data);
			mmsghdr_iovecs_len.set(msgvec, 0L, i, 1);
			mmsghdr_control.set(msgvec, 0L, i, MemorySegment.NULL);
			mmsghdr_control_len.set(msgvec, 0L, i, 0);
			mmsghdr_flags.set(msgvec, 0L, i, 0);
			mmsghdr_msg_len.set(msgvec, 0L, i, 0);
		}
	}

	public void bind(DatagramChannel channel) throws Exception {
		//Channel must be already bound

		var fdField = channel.getClass().getDeclaredField("fd");
		fdField.setAccessible(true);
		var fd = (FileDescriptor)fdField.get(channel);
		var fdIntField = fd.getClass().getDeclaredField("fd");
		fdIntField.setAccessible(true);
		fdi = (int)fdIntField.get(fd);
	}

	/** Connects this sender to the specified addresses. */
	public void connect(Seq<InetSocketAddress> addresses){
		//Write the provided addresses to the sockaddr structs
		numConnectedAddresses = addresses.size;
		for(int i = 0; i < numConnectedAddresses; i ++){
			sockaddr_port.set(dest_addr_array, 0L, (long)i, (short)addresses.get(i).getPort());
			//HACK: Inet4Address does not expose its address, except through the getBytes method, but this allocates a new byte[].
			//We can't afford to allocate on every single packet. Reflection would be too slow.
			//However, the hashcode method happens to return the address, so...
			sockaddr_address.set(dest_addr_array, 0L, (long)i, addresses.get(i).getAddress().hashCode());
		}
	}
	/** Connects this sender to the specified addresses. */
	public void connect(Connection[] connections){
		//Write the provided addresses to the sockaddr structs
		int j = 0;
		for(int i = 0; i < connections.length; i ++){
			var addr = connections[i].udpRemoteAddress;
			if(addr == null) continue;
			Log.debug(addr);
			sockaddr_port.set(dest_addr_array, 0L, (long)j, (short)addr.getPort());
			//HACK: see above method
			sockaddr_address.set(dest_addr_array, 0L, (long)j, addr.getAddress().hashCode());
			j ++;
		}
		numConnectedAddresses = j;
	}
	/** Connects this sender to the UDP addresses of the specified connections. */
	public void connect(Iterable<Connection> connections){ //TODO: memory corruption occurs if connections is bigger than maxSimultaneousAddresses
		//Write the provided addresses to the sockaddr structs
		numConnectedAddresses = 0;
		for(var con : connections){
			var addr = con.udpRemoteAddress;
			if(addr == null) continue;
			sockaddr_port.set(dest_addr_array, 0L, (long)numConnectedAddresses, (short)addr.getPort());
			//HACK: see above method
			sockaddr_address.set(dest_addr_array, 0L, (long)numConnectedAddresses, addr.getAddress().hashCode());
			numConnectedAddresses ++;
		}
	}
	
	/** Returns the number of addresses that the message was sent to, or throws an exception. */
	public int send(ByteBuffer buffer) {
		//Point the iovec at the provided ByteBuffer
		buffer.rewind();
		var dataSize = buffer.remaining();
		Log.info(dataSize);
		iovec_ptr.set(data, 0L, MemorySegment.ofBuffer(buffer));
		iovec_len.set(data, 0L, dataSize);

		int sent;
		try {
			Log.info("fdi: @, numConnectedAddresses: @", fdi, numConnectedAddresses);
			sent = (int) sendmmsg.invokeExact(captureState, fdi, msgvec, numConnectedAddresses, 0);
		} catch(Throwable t){
			//this should never happen
			throw new RuntimeException(t);
		}
		if(sent == -1){
			throw new RuntimeException("sendmmsg returned -1, errno is " + errno.get(captureState, 0L));
		}
		return sent;
	}

	public void close(){
		//Free all allocated structs
		arena.close();
	}
}