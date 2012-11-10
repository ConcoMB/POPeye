package connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import nio.server.DoubleBuffer;

import proxy.Popeye;
import proxy.Writeable;

public class Connection {
	protected DoubleBuffer clientBuffer;
	private SocketChannel client;
	
	public Connection(int bufSize, Writeable out, SocketChannel client) throws IOException{
		this.clientBuffer=new DoubleBuffer(bufSize);	
		this.client=client;
	}

	
	
	public DoubleBuffer getClientBuffer() {
		return clientBuffer;
	}
	
	public SocketChannel getClient(){
		return client;
	}
	
	public boolean isClient(SocketChannel channel){
		return client.equals(channel);
	}
	
	public void setClient(SocketChannel client){
		this.client=client;
	}
}