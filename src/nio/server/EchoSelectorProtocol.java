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
		DoubleBuffer dBuf=new DoubleBuffer(bufSize);
		hostChan.register(selector, SelectionKey.OP_READ, dBuf);
		System.out.println("client:"+clntChan);
		System.out.println("host:"+hostChan);
		clientMap.put(hostChan, clntChan);
		serverMap.put(clntChan, hostChan);
    }

    private boolean isServer(SocketChannel channel){
    	return serverMap.containsValue(channel);
    }
    
    public void handleRead(SelectionKey key) throws IOException {
        // Client socket channel has pending data
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuffer sBuf = ((DoubleBuffer) key.attachment()).getReadBuffer();
        ByteBuffer buf=ByteBuffer.allocate(bufSize);
        long bytesRead = channel.read(buf);
        buf.flip();
        if (bytesRead == -1) { // Did the other end close?
            channel.close();
        } else if (bytesRead > 0) {
        	String line=BufferUtils.bufferToString(buf);
        	sBuf.append(line);
        	if(!line.endsWith("\r\n")){
        		return;
        	}
        	line=sBuf.toString();
        	System.out.print("READ:"+bytesRead+" "+line);
        	if(isServer(channel)){
        		//SERVER
        		/*for(String s: line.split("\r\n")){
        			proxyMap.get(clientMap.get(channel)).proxyServer(s.concat("\r\n"));
    			}*/
        		proxyMap.get(clientMap.get(channel)).proxyServer(line);
        	}else{
        		//CLIENT
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
        			/*for(String s: line.split("\r\n")){
        				proxyMap.get(channel).proxyClient(s.concat("\r\n"));
        			}*/
        			proxyMap.get(channel).proxyClient(line);
        		}
        	}
        }
    }

    public void handleWrite(SelectionKey key) throws IOException {
        /*
         * Channel is available for writing, and key is valid (i.e., client
         * channel not closed).
         */
        // Retrieve data read earlier
        StringBuffer sBuf = ((DoubleBuffer) key.attachment()).getWriteBuffer();
        //buf.flip(); // Prepare buffer for writing
        SocketChannel channel = (SocketChannel) key.channel();
        //System.out.println("write ("+sBuf+")");
        //buf.flip();
        ByteBuffer buf=ByteBuffer.wrap(sBuf.toString().getBytes());
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
		System.out.print("S--> "+line);
		writeToChannel(client,line);
	}

	@Override
	public void writeToServer(SocketChannel client, String line) throws IOException {
		System.out.print("C--> "+line);
		SocketChannel server=serverMap.get(client);
		writeToChannel(server, line);
	}
	
	private void writeToChannel(SocketChannel channel, String line) throws CharacterCodingException{
		SelectionKey key=channel.keyFor(selector);
		StringBuffer sBuf=((DoubleBuffer) key.attachment()).getWriteBuffer();
		String before=sBuf.toString();
		/*if(buf.hasRemaining()){
			buf.compact();
		}*/
		sBuf.append(line);
		String after=sBuf.toString();
		key.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
	}
}