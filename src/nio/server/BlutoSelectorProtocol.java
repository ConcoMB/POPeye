package nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import proxy.Writeable;
import configuration.Configuration;

public class BlutoSelectorProtocol implements SelectorProtocol, Writeable {
    private int bufSize; // Size of I/O buffer
	private Selector selector;
	private HashMap<SocketChannel,Configuration> configMap=new HashMap<SocketChannel,Configuration>();

    public BlutoSelectorProtocol(int bufSize, Selector selector) {
        this.bufSize = bufSize;
        this.selector=selector;
    }

    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
        clntChan.configureBlocking(false); // Must be nonblocking to register
        // Register the selector with new channel for read and attach byte
        // buffer
        System.out.println("BLUTO: Accepted connection ->"+clntChan.socket().getRemoteSocketAddress());
        configMap.put(clntChan, new Configuration(this,clntChan));
        clntChan.register(key.selector(), SelectionKey.OP_READ, new DoubleBuffer(bufSize));
    }
    
    public void handleRead(SelectionKey key) throws IOException, InterruptedException {
        // Client socket channel has pending data
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuffer sBuf = ((DoubleBuffer) key.attachment()).getReadBuffer();
        ByteBuffer buf=ByteBuffer.allocate(bufSize);
        long bytesRead = channel.read(buf);
        buf.flip();
        if (bytesRead == -1) { // Did the other end close?
    		System.out.println("BLUTO: Client disconnected:"+channel.socket().getRemoteSocketAddress());
        	disconnectClient(channel);
        } else if (bytesRead > 0) {
        	String line=BufferUtils.bufferToString(buf);
        	sBuf.append(line);
        	if(!line.endsWith("\r\n")){
        		return;
        	}
        	line=sBuf.toString();
        	System.out.println("BLUTO: C--> "+line);
        	sBuf.delete(0, sBuf.length());
        	//System.out.print("READ:"+bytesRead+" "+line);
        	configMap.get(channel).apply(line.trim());
        }
    }

    private void disconnectClient(SocketChannel client) throws IOException {
    	configMap.remove(client);
    	client.close();
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

	@Override
	public void writeToClient(SocketChannel client, String line) throws IOException, InterruptedException {
		String message=line.length()>30?line.substring(0, 30)+"...\n":line;
		System.out.print("BLUTO: S--> "+message);
		writeToChannel(client,line);
	}

	@Override
	public void writeToServer(SocketChannel client, String line) throws IOException, InterruptedException {
		//NADA
	}
	
	private void writeToChannel(SocketChannel channel, String line) throws InterruptedException, IOException{
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
