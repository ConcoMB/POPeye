package service;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import nio.server.ExternalAppExecuter;
import proxy.Popeye;
import proxy.Writeable;
import proxy.transform.AnonymousTransformer;
import proxy.transform.ImageRotationTransformer;
import proxy.transform.VowelTransformer;
import service.Brutus.BrutusVariable;
import user.Statistics;
import user.User;

public class Olivia extends Service{
	
	private static int bytesTransferred, successfulConnections, connections;

	private enum OliviaCommand{
		BYTES, CONNECTIONS, FULL,SUCCESSFUL_CONNECTIONS, FAILED_CONNECTIONS, EMAILS_READ, EMAILS_ERASED, ERASE_FAILURES,
		CHECK_VAR;
	}

	public Olivia(Writeable out, SocketChannel channel){
		super(out, channel);
	}


	public void consult(String line) throws IOException, InterruptedException{
		String[] command = line.split(" ");
		if(command.length==1){
			if(command.equals("QUIT")){
				byebye();
				return;
			}
		}
		if(command.length!=4){
			invalidConfig();
		}
		String ans="";
		OliviaCommand c;
		BrutusVariable v = null;
		if(!command[0].equals("IN") || !command[2].equals("ASK")){
			invalidConfig();
			return;
		}
		try{
			c = OliviaCommand.valueOf(command[3]);
		}catch(Exception e){
			try{
				v=BrutusVariable.valueOf(command[3]);
				c=OliviaCommand.CHECK_VAR;
			}catch(Exception e2){
				invalidConfig();				
				return;
			}
		}
		if(command[1].equals("GENER@L")){
			switch(c){
			case BYTES:
				writeSimple(bytesTransferred);
				break;
			case CONNECTIONS:
				writeSimple(connections);
				break;
			case SUCCESSFUL_CONNECTIONS:
				writeSimple(successfulConnections);
				break;
			case FAILED_CONNECTIONS:
				writeSimple(successfulConnections-connections);
				break;
			case FULL:
				writeFullStats();
				break;
			default:
				//EROR
				invalidConfig();				
				return;
			}
		}else{
			User user = Popeye.getUserByName(command[1].trim());
			if(user==null){
				//ERROR
				invalidConfig("no info about user");
				return;
			}
			Statistics stats = user.getStats();
			switch (c){
			case BYTES:
				writeSimple(stats.getBytesTransferred());
				break;
			case CONNECTIONS:
				writeSimple(stats.getAccesses());
				break;
			case SUCCESSFUL_CONNECTIONS:
				writeSimple(stats.getAccesses()-stats.getAccessFailures());
				break;
			case FAILED_CONNECTIONS:
				writeSimple(stats.getAccessFailures());
				break;
			case EMAILS_READ:
				writeSimple(stats.getEmailsRead());
				break;
			case EMAILS_ERASED:
				writeSimple(stats.getEmailsErased());
				break;
			case ERASE_FAILURES:
				writeSimple(stats.getEraseFailures());
				break;
			case FULL:
				writeFullStats(stats);
				break;
			case CHECK_VAR:
				switch(v){

				case MINHOUR:
					writeSimple(user.getHourDenial().getMinHour() +":"+user.getHourDenial().getMinMinute());
					break;
				case MAXHOUR:
					writeSimple(user.getHourDenial().getMaxHour() +":"+user.getHourDenial().getMaxMinute());
					break;
				case QUANT: 
					writeSimple(""+user.getQuantityDenial().getTop());
					break;
				case SERVER: 
					ans = user.getServer();
					if(ans==null){
						writeSimple("default server");
					}else{
						writeSimple(ans);
					}
					break;
				case ERASE_DATE: 
					writeSimple(""+user.getEraseConditions().getDateLimitFrom());
					break;
				case ERASE_FROM:
					for(String s: user.getEraseConditions().getFrom()){
						ans+=(s+"\n");
					}
					if(ans.equals("")){
						writeSimple("no conditions");
					}else{
						writeSimple(ans);
					}
					break;
				case ERASE_CONTENTTYPE:
					for(String s: user.getEraseConditions().getContentTypes()){
						ans+=(s+"\n");
					}
					if(ans.equals("")){
						writeSimple("no conditions");
					}else{
						writeSimple(ans);
					}
					break;
				case ERASE_MINSIZE: 
					writeSimple(user.getEraseConditions().getMinSize()+"");
					break;
				case ERASE_MAXSIZE: 
					writeSimple(user.getEraseConditions().getMaxSize()+"");
					break;
				case ERASE_ATTACHMENT:
					int i = user.getEraseConditions().getWithAttachment();
					if(i==1){
						writeSimple("must have attachment");
					}else if(i==0){
						writeSimple("no conditions");
					}else{
						writeSimple("mustn't have attachment");
					}
					break;
				case ERASE_PICTURE:
					i = user.getEraseConditions().getWithPicture();
					if(i==1){
						writeSimple("must have pictures");
					}else if(i==0){
						writeSimple("no conditions");
					}else{
						writeSimple("mustn't have pictures");
					}
					break;
				case ANONYMOUS_T:
					writeSimple(user.getTransformers().contains(AnonymousTransformer.getInstance())?"yes":"no");
					break;
				case IMAGE_T:
					writeSimple(user.getTransformers().contains(ImageRotationTransformer.getInstance())?"yes":"no");

					break;
				case VOWELS_T:
					writeSimple(user.getTransformers().contains(VowelTransformer.getInstance())?"yes":"no");
					break;
				case APP:
					ExternalAppExecuter app=user.getApp();
					if(app==null){
						writeSimple("no application set for this user");
					}else{
						writeSimple("Application path: "+app.getPath());
					}
					break;
				default:
					//ERROR;
					invalidConfig();					
					break;
				}
				break;

			default:
				//EROR
				invalidConfig();				
				return;	
			}

		}
	}
	private void writeFullStats(Statistics stats) throws IOException, InterruptedException{
		writeOK();
		writeSimple("Connections: " + stats.getAccesses() );
		writeSimple("Connections failed : " + stats.getAccessFailures() );
		writeSimple("Bytes transferred: " + stats.getBytesTransferred() );
		writeSimple("Emails erased: " + stats.getEmailsErased() );
		writeSimple("Emails read: " + stats.getEmailsRead() );
		writeSimple("Erase failures: " + stats.getEraseFailures() );
		writeEndMultiline();
	}

	private void writeFullStats() throws IOException, InterruptedException{
		writeOK();
		writeSimple("Connections: " + connections);
		writeSimple("Connections failed : " + successfulConnections);
		writeSimple("Bytes transferred: " + bytesTransferred);
		writeEndMultiline();
	}


	public static void addConnection() {
		connections++;
	}
	
	public static void addSuccessfulConnection() {
		successfulConnections++;
	}
	
	public static void addBytes(int bytes) {
		bytesTransferred+=bytes;
	}
}
