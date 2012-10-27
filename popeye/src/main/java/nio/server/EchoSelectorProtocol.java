package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
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
	private Map<SocketChannel,ExternalAppExecuter> appMap=new HashMap<SocketChannel,ExternalAppExecuter>();
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
        connectToServer(clntChan, "pop3.alu.itba.edu.ar");
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
		appMap.put(clntChan, new ExternalAppExecuter("/home/fede/git/POPeye/apps/echo.o"));
    }

    private boolean isServer(SocketChannel channel){
    	return serverMap.containsValue(channel);
    }
    
    public void handleRead(SelectionKey key) throws IOException, InterruptedException, ParseException {
        // Client socket channel has pending data
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuffer sBuf = ((DoubleBuffer) key.attachment()).getReadBuffer();
        ByteBuffer buf=ByteBuffer.allocate(bufSize);
        long bytesRead = channel.read(buf);
        buf.flip();
        if (bytesRead == -1) { // Did the other end close?
        	if(isServer(channel)){
        		//SERVER DISCONNECTED
        		System.out.println("Server disconnected (client:"+clientMap.get(channel).socket().getRemoteSocketAddress()+")");
        		clientMap.get(channel).close();
        	}else{
        		//CLIENT DISCONNECTED
        		System.out.println("Client disconnected:"+channel.socket().getRemoteSocketAddress());
        		serverMap.get(channel).close();
        	}
        	channel.close();
        } else if (bytesRead > 0) {
        	String line=BufferUtils.bufferToString(buf);
        	sBuf.append(line);
        	if(!line.endsWith("\r\n")){
        		return;
        	}
        	line=sBuf.toString();
        	sBuf.delete(0, sBuf.length());
        	//System.out.print("READ:"+bytesRead+" "+line);
        	if(isServer(channel))
				try {
					{
						//SERVER
						/*for(String s: line.split("\r\n")){
							proxyMap.get(clientMap.get(channel)).proxyServer(s.concat("\r\n"));
						}*/
						proxyMap.get(clientMap.get(channel)).proxyServer(line);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else{
        		//CLIENT
        		//TODO turbio
        		//if(serverMap.get(channel)==null){
        		if(line.startsWith("USER")){
        			//AUTHENTICATION
        			System.out.println("AUTHENTICATION");
        			String serverName=proxyMap.get(channel).login(line);
        			if(serverName==null){
        				//FAIL
        				writeToClient(channel, "-ERR");
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
        sBuf.delete(0, bytesWritten);
        buf.compact(); // Make room for more data to be read in
        buf.clear();
    }

	public void writeToClient(SocketChannel client, String line) throws IOException, InterruptedException {
		String message=line.length()>30?line.substring(0, 30)+"...\n":line;
		System.out.print("S--> "+message);
		writeToChannel(client,line);
	}

	public void writeToServer(SocketChannel client, String line) throws IOException, InterruptedException {
		String message=line.length()>30?line.substring(0, 30)+"...\n":line;
		System.out.print("C--> "+message);
		SocketChannel server=serverMap.get(client);
		writeToChannel(server, line);
	}
	
	private void writeToChannel(SocketChannel channel, String line) throws InterruptedException, IOException{
		ExternalAppExecuter appExec=appMap.get(channel);
		if(appExec!=null){
			line=appExec.execute(line);
		}
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