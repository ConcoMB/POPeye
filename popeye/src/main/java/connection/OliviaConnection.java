package connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import proxy.Writeable;
import service.Olivia;

public class OliviaConnection extends Connection{
	private Olivia olivia;
	
	public OliviaConnection(int bufSize, Writeable out, SocketChannel client)
			throws IOException {
		super(bufSize, out, client);
		this.olivia=new Olivia(out, this);
	}
	
	public Olivia getOlivia(){
		return olivia;
	}
}
