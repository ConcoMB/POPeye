package proxy;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface Writeable {

	public void writeToClient(SocketChannel client, String line) throws IOException ;
	
	public void writeToServer(SocketChannel client, String line) throws IOException;
	
}
