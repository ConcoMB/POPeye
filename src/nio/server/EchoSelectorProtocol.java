package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import proxy.POPeye;
import proxy.Writeable;

public class EchoSelectorProtocol implements TCPProtocol, Writeable {
    private int bufSize; // Size of I/O buffer
	private int defaultPort;
	private Map<SocketChannel,SocketChannel> clientMap=new HashMap<SocketChannel,SocketChannel>();
	private Map<SocketChannel,SocketChannel> serverMap=new HashMap<SocketChannel,SocketChannel>();
	private Map<SocketChannel,POPeye> proxyMap=new HashMap<SocketChannel,POPeye>();
	private Selector selector;

    public EchoSelectorProtocol(int bufSize, int dp, Selector selector) {
        this.bufSize = bufSize;
        this.defaultPort=dp;
        this.selector=selector;
    }

    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
        clntChan.configureBlocking(false); // Must be nonblocking to register
        // Register the selector with new channel for read and attach byte
        // buffer
        System.out.println("Accepted connection ->"+clntChan.socket().getRemoteSocketAddress());
        clntChan.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufSize));
        proxyMap.put(clntChan, new POPeye(this,clntChan));
    }
    
    private void connectToServer(SocketChannel clntChan, String serverName) throws IOException{
    	SocketChannel hostChan = SocketChannel.open(new InetSocketAddress(serverName, defaultPort));
		hostChan.configureBlocking(false); // Must be nonblocking to register
		System.out.println("Creating connection ->"+hostChan.socket().getRemoteSocketAddress());
		hostChan.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(bufSize));
		System.out.println("client:"+clntChan);
		System.out.println("host:"+hostChan);
		clientMap.put(hostChan, clntChan);
		serverMap.put(clntChan, hostChan);
    }

    public void handleRead(SelectionKey key) throws IOException {
        // Client socket channel has pending data
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buf = (ByteBuffer) key.attachment();
        long bytesRead = channel.read(buf);
        if (bytesRead == -1) { // Did the other end close?
            channel.close();
        } else if (bytesRead > 0) {
        	String line=BufferUtils.bufferToString(buf);
        	System.out.print("READ: ("+line+")");
        	if(serverMap.containsValue(channel)){
        		//SERVER
        		System.out.println("from server");
        		proxyMap.get(channel).proxyServer(line);
        	}else{
        		//CLIENT
        		System.out.println("from client");
        		if(serverMap.get(channel)==null){
        			//AUTHENTICATION
        			String serverName=proxyMap.get(channel).login(line);
        			if(serverName==null){
        				//FAIL
        				writeToClient(channel, "err");
        				return;
        			}else{
        				connectToServer(channel, serverName);
        			}
        		}else{
        			//NORMAL FLOW
        			proxyMap.get(channel).proxyClient(line);
        		}
        	}
        	//ByteBuffer echo=ByteBuffer.wrap(buf.array());
        	//other.write(echo);
        	//other.register(key.selector(), SelectionKey.OP_WRITE|SelectionKey.OP_READ,echo);
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
        SocketChannel channel = (SocketChannel) key.channel();
        //System.out.println("write ("+BufferUtils.bufferToString(buf)+")");
        channel.write(buf);
        if (!buf.hasRemaining()) { // Buffer completely written?
            // Nothing left, so no longer interested in writes
            key.interestOps(SelectionKey.OP_READ);
        }
        buf.compact(); // Make room for more data to be read in
    }

	@Override
	public void writeToClient(SocketChannel client, String line) throws IOException {
		client.write(ByteBuffer.wrap(line.getBytes()));
		handleWrite(client.keyFor(selector));
	}

	@Override
	public void writeToServer(SocketChannel client, String line) throws IOException {
		SocketChannel server=serverMap.get(client);
		server.write(ByteBuffer.wrap(line.getBytes()));
		handleWrite(server.keyFor(selector));
	}
}