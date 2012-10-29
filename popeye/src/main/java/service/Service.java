package service;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import proxy.Writeable;
import user.Statistics;

public abstract class Service {

	private SocketChannel channel;
	private Writeable out;
	private final static String OK=":)", ERROR=":(";
	
	protected Service(Writeable out, SocketChannel channel){
		this.channel=channel;
		this.out=out;
	}
	

	protected void writeOK() throws IOException, InterruptedException{
		writeSimple(OK);
	}
	
	protected void writeSimple(int info) throws IOException, InterruptedException{
		writeSimple(""+info);
	}

	protected void writeSimple(String info) throws IOException, InterruptedException{
		out.writeToClient(channel, info+"\n");
	}

	protected void writeEndMultiline() throws IOException, InterruptedException{
		writeSimple(".");
	}
	
	protected void invalidConfig() throws IOException, InterruptedException {
		invalidConfig("invalid config");
	}
	
	protected void invalidConfig(String s) throws IOException, InterruptedException{
		out.writeToClient(channel, ERROR+" "+ s+"\r\n");
		System.out.println(ERROR+" "+s);
	}
	
	
}
