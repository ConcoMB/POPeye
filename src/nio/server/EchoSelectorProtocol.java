package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
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
        clntChan.register(key.selector(), SelectionKey.OP_READ, new DoubleBuffer(bufSize));
        proxyMap.put(clntChan, new POPeye(this,clntChan));
        connectToServer(clntChan, "pop.aol.com");
    }
    
    private void connectToServer(SocketChannel clntChan, String serverName) throws IOException{
    	SocketChannel hostChan = SocketChannel.open(new InetSocketAddress(serverName, defaultPort));
		hostChan.configureBlocking(false); // Must be nonblocking to register
		System.out.println("Creating connection ->"+hostChan.socket().getRemoteSocketAddress());
		hostChan.register(selector, SelectionKey.OP_READ, new DoubleBuffer(bufSize));
		System.out.println("client:"+clntChan);
		System.out.println("host:"+hostChan);
		clientMap.put(hostChan, clntChan);
		serverMap.put(clntChan, hostChan);
    }

    public void handleRead(SelectionKey key) throws IOException {
        // Client socket channel has pending data
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buf = ((DoubleBuffer) key.attachment()).getReadBuffer();
        buf.rewind();
        long bytesRead = channel.read(buf);
        buf.rewind();
        if (bytesRead == -1) { // Did the other end close?
            channel.close();
        } else if (bytesRead > 0) {
        	String line=BufferUtils.bufferToString(buf);
        	System.out.print("READ:"+bytesRead+" ("+line+")");
        	if(serverMap.containsValue(channel)){
        		//SERVER
        		System.out.println("from server");
        		for(String s: line.split("\r\n")){
        			proxyMap.get(clientMap.get(channel)).proxyServer(s.concat("\n"));
    			}
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
        				writeToServer(channel, line);
        			}
        		}else{
        			//NORMAL FLOW
        			for(String s: line.split("\r\n")){
        				proxyMap.get(channel).proxyClient(s.concat("\n"));
        			}
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
        ByteBuffer buf = ((DoubleBuffer) key.attachment()).getWriteBuffer();
        //buf.flip(); // Prepare buffer for writing
        SocketChannel channel = (SocketChannel) key.channel();
        System.out.println("write ("+BufferUtils.bufferToString(buf)+")");
        buf.flip();
        int bytesWritten=channel.write(buf);
        if (!buf.hasRemaining()) { // Buffer completely written?
        	//System.out.println("wrote all");
        	//System.out.println(BufferUtils.bufferToString(buf));
            // Nothing left, so no longer interested in writes
            key.interestOps(SelectionKey.OP_READ);
        }
        buf.compact(); // Make room for more data to be read in
        buf.clear();
    }

	@Override
	public void writeToClient(SocketChannel client, String line) throws IOException {
		System.out.println("toClient:("+line+")");
		writeToChannel(client,line);
	}

	@Override
	public void writeToServer(SocketChannel client, String line) throws IOException {
		System.out.println("toServer:("+line+")");
		SocketChannel server=serverMap.get(client);
		writeToChannel(server, line);
	}
	
	private void writeToChannel(SocketChannel channel, String line){
		SelectionKey key=channel.keyFor(selector);
		ByteBuffer buf=((DoubleBuffer) key.attachment()).getWriteBuffer();
		buf.put(line.getBytes());
		key.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
	}
}