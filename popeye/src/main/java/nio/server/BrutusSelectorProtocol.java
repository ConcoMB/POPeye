package nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import proxy.Writeable;
import service.Brutus;
import connection.BrutusConnection;
import connection.Connection;

public class BrutusSelectorProtocol implements SelectorProtocol, Writeable {
    private int bufSize; // Size of I/O buffer
	private Selector selector;

    public BrutusSelectorProtocol(int bufSize, Selector selector) {
        this.bufSize = bufSize;
        this.selector=selector;
    }

    public void handleAccept(SelectionKey key) throws IOException, InterruptedException {
        SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
        clntChan.configureBlocking(false); // Must be nonblocking to register
        // Register the selector with new channel for read and attach byte
        // buffer
        System.out.println("BRUTUS: Accepted connection ->"+clntChan.socket().getRemoteSocketAddress());
        BrutusConnection con = new BrutusConnection(bufSize,this,clntChan);
        clntChan.register(key.selector(), SelectionKey.OP_READ, con);
        writeToClient(con, ":) Brutus says hi\r\n");
        writeToClient(con, "Password:");
    }
    
    public void handleRead(SelectionKey key) throws IOException, InterruptedException {
    	BrutusConnection con = ((BrutusConnection) key.attachment());
        // Client socket channel has pending data
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuffer sBuf = con.getClientBuffer().getReadBuffer();
        ByteBuffer buf=ByteBuffer.allocate(bufSize);
        long bytesRead = channel.read(buf);
        buf.flip();
        if (bytesRead == -1) { // Did the other end close?
    		System.out.println("BRUTUS: Connecion disconnected:"+channel.socket().getRemoteSocketAddress());
        	disconnectClient(channel);
        } else if (bytesRead > 0) {
        	String line=BufferUtils.bufferToString(buf);
        	sBuf.append(line);
        	if(!line.endsWith("\r\n")){
        		return;
        	}
        	line=sBuf.toString();
        	System.out.println("BRUTUS: "+line);
        	sBuf.delete(0, sBuf.length());
        	//System.out.print("READ:"+bytesRead+" "+line);
        	con.getBrutus().apply(line.trim());
        }
    }

    private void disconnectClient(SocketChannel client) throws IOException {
    	client.close();
	}
    
    public void handleWrite(SelectionKey key) throws IOException {
    	BrutusConnection con = ((BrutusConnection) key.attachment());
        /*
         * Channel is available for writing, and key is valid (i.e., client
         * channel not closed).
         */
        // Retrieve data read earlier
        StringBuffer sBuf = con.getClientBuffer().getWriteBuffer();
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

	public void writeToClient(Connection con, String line) throws IOException, InterruptedException {
		String message=line.length()>30?line.substring(0, 30)+"...\n":line;
//		System.out.print("BRUTUS: S--> "+message);
		writeToChannel(con.getClient(),line, con.getClientBuffer().getWriteBuffer());
	}

	public void writeToServer(Connection con, String line) throws IOException, InterruptedException {
		//NADA
	}
	
	private void writeToChannel(SocketChannel channel, String line, StringBuffer sBuf) throws InterruptedException, IOException{
		SelectionKey key=channel.keyFor(selector);
		String before=sBuf.toString();
		/*if(buf.hasRemaining()){
			buf.compact();
		}*/
		sBuf.append(line);
		String after=sBuf.toString();
		key.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
	}
}
