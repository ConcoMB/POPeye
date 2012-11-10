package proxy;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import connection.Connection;


public interface Writeable {

	public void writeToClient(Connection connection, String line) throws IOException, InterruptedException;
	
	public void writeToServer(Connection connection, String line) throws IOException, InterruptedException;
}
