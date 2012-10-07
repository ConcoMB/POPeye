package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class EchoSelectorProtocol implements TCPProtocol {
    private int bufSize; // Size of I/O buffer
    private String defaultHost;
	private int defaultPort;
	private Map<SocketChannel,SocketChannel> cMap=new HashMap<SocketChannel,SocketChannel>();

    public EchoSelectorProtocol(int bufSize,String dh, int dp) {
        this.bufSize = bufSize;
        this.defaultHost=dh;
        this.defaultPort=dp;
    }

    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
        clntChan.configureBlocking(false); // Must be nonblocking to register
        // Register the selector with new channel for read and attach byte
        // buffer
        System.out.println("Accepted connection ->"+clntChan.socket().getRemoteSocketAddress());
        clntChan.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufSize));
        SocketChannel hostChan = SocketChannel.open(new InetSocketAddress(defaultHost, defaultPort));
		hostChan.configureBlocking(false); // Must be nonblocking to register
		System.out.println("Creating connection ->"+hostChan.socket().getRemoteSocketAddress());
		hostChan.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufSize));
		System.out.println("client:"+clntChan);
		System.out.println("host:"+hostChan);
		cMap.put(hostChan, clntChan);
		cMap.put(clntChan, hostChan);
    }

    public void handleRead(SelectionKey key) throws IOException {
        // Client socket channel has pending data
        SocketChannel clntChan = (SocketChannel) key.channel();
        SocketChannel other = cMap.get(clntChan);
        ByteBuffer buf = (ByteBuffer) key.attachment();
        long bytesRead = clntChan.read(buf);
        if (bytesRead == -1) { // Did the other end close?
            clntChan.close();
        } else if (bytesRead > 0) {
        	System.out.println("READ: "+BufferUtils.bufferToString(buf)+")");
        	ByteBuffer echo=ByteBuffer.wrap(buf.array());
        	other.write(echo);
        	other.register(key.selector(), SelectionKey.OP_WRITE|SelectionKey.OP_READ,echo);
        	//other.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            // Indicate via key that reading/writing are both of interest now.
            //key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    public void handleWrite(SelectionKey key) throws IOException {
        /*
         * Channel is available for writing, and key is valid (i.e., client
         * channel not closed).
         */
        // Retrieve data read earlier
        ByteBuffer buf = (ByteBuffer) key.attachment();
        buf.flip(); // Prepare buffer for writing
        SocketChannel clntChan = (SocketChannel) key.channel();
        //System.out.println("write ("+BufferUtils.bufferToString(buf)+")");
        clntChan.write(buf);
        if (!buf.hasRemaining()) { // Buffer completely written?
            // Nothing left, so no longer interested in writes
            key.interestOps(SelectionKey.OP_READ);
        }
        buf.compact(); // Make room for more data to be read in
    }
}