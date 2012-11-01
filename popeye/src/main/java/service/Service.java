package service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import proxy.Writeable;

public abstract class Service {

	private SocketChannel channel;
	private Writeable out;
	private boolean connected;
	protected final static String OK=":)";
	protected static final String ERROR=":(";
	protected static String password;
	
	protected Service(Writeable out, SocketChannel channel) throws IOException{
		this.channel=channel;
		this.out=out;
		File f = new File("./servicePassword.conf");
		System.out.println("file" +f.getAbsolutePath());
		BufferedReader b = new BufferedReader(new FileReader(f));
		String line = b.readLine();
		password = line.split("password = ")[1];
		System.out.println("Services password: "+ password);
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
	
	protected void byebye() throws IOException, InterruptedException{
		writeSimple(OK+" byebye!");
		channel.close();
	}
	
	protected boolean handleConnection(String command) throws IOException, InterruptedException{
		if(command.equals("QUIT")){
			return true;
		}
		if(!connected){
			if(command.equals(password)){
				connected=true;
				writeOK();
				return false;
			}else{
				out.writeToClient(channel, ERROR+" Password:");
				return false;
			}
		}
		return true;
	}
}
