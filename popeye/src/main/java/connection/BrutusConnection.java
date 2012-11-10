package connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import proxy.Writeable;
import service.Brutus;

public class BrutusConnection extends Connection{
	private Brutus brutus;
	
	public BrutusConnection(int bufSize, Writeable out, SocketChannel client)
			throws IOException {
		super(bufSize, out, client);
		this.brutus=new Brutus(out, this);
	}
	
	public Brutus getBrutus(){
		return brutus;
	}
}