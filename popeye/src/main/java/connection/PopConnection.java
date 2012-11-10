package connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import nio.server.DoubleBuffer;
import proxy.Popeye;
import proxy.Writeable;

public class PopConnection extends Connection {
	private boolean connection;
	private Popeye proxy;
	private DoubleBuffer serverBuffer;
	private SocketChannel server;
	
	public PopConnection(int bufSize, Writeable out, SocketChannel client) throws IOException{
		super(bufSize, out, client);
		this.connection=true;
		proxy=new Popeye(out, this);
		this.serverBuffer=new DoubleBuffer(bufSize);
	}
	
	public void setConnection(boolean connection){
		this.connection = connection;
	}
		
	public boolean getConnection(){
		return connection;
	}
	
	public DoubleBuffer getServerBuffer() {
		return serverBuffer;
	}
	
	public boolean isServer(SocketChannel channel){
		return server.equals(channel);
	}
	
	public SocketChannel getServer(){
		return server;
	}
	
	public void setServer(SocketChannel server){
		this.server=server;
	}
	
	public Popeye getProxy(){
		return proxy;
	}

	public DoubleBuffer getBuffer(SocketChannel channel) {
		if(isServer(channel)){
			return serverBuffer;
		}
		return clientBuffer;
	}
}
