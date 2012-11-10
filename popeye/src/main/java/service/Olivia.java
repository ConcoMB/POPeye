package service;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Set;

import connection.Connection;
import connection.OliviaConnection;

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
		BYTES, CONNECTIONS, FULL,SUCCESSFUL_CONNECTIONS, FAILED_CONNECTIONS, EMAILS_READ, EMAILS_ERASED, ERASE_FAILURES, APPS,
		CHECK_VAR;
	}

	public Olivia(Writeable out, Connection con) throws IOException{
		super(out, con);
	}


	public void consult(String line) throws IOException, InterruptedException{
		if(!handleConnection(line)){
			return;
		}
		String[] command = line.split(" ");
		if(command.length==1){
			if(line.equals("QUIT")){
				byebye();
				return;
			}
		}
		if(command.length!=4){
			invalidConfig();
			return;
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
				writeSimple(OK+" "+bytesTransferred);
				break;
			case CONNECTIONS:
				writeSimple(OK+" "+connections);
				break;
			case SUCCESSFUL_CONNECTIONS:
				writeSimple(OK+" "+successfulConnections);
				break;
			case FAILED_CONNECTIONS:
				writeSimple(OK+" "+(successfulConnections-connections));
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
				writeSimple(OK+" "+stats.getBytesTransferred());
				break;
			case CONNECTIONS:
				writeSimple(OK+" "+stats.getAccesses());
				break;
			case SUCCESSFUL_CONNECTIONS:
				writeSimple(OK+" "+(stats.getAccesses()-stats.getAccessFailures()));
				break;
			case FAILED_CONNECTIONS:
				writeSimple(OK+" "+stats.getAccessFailures());
				break;
			case EMAILS_READ:
				writeSimple(OK+" "+stats.getEmailsRead());
				break;
			case EMAILS_ERASED:
				writeSimple(OK+" "+stats.getEmailsErased());
				break;
			case ERASE_FAILURES:
				writeSimple(OK+" "+stats.getEraseFailures());
				break;
			case FULL:
				writeFullStats(stats);
				break;
			case APPS:
				Set<ExternalAppExecuter> apps=user.getApps();
				if(apps.size()==0){
					writeSimple(OK+" "+"no application set for this user");
				}else{
					writeOK();
					for(ExternalAppExecuter app: apps){
						writeSimple(app.getPath());
					}
				}
				writeEndMultiline();
				break;
			case CHECK_VAR:
				switch(v){

				case MINHOUR:
					writeSimple(OK+" "+user.getHourDenial().getMinHour() +":"+user.getHourDenial().getMinMinute());
					break;
				case MAXHOUR:
					writeSimple(OK+" "+user.getHourDenial().getMaxHour() +":"+user.getHourDenial().getMaxMinute());
					break;
				case QUANT: 
					writeSimple(OK+" "+user.getQuantityDenial().getTop());
					break;
				case SERVER: 
					ans = user.getServer();
					if(ans==null){
						writeSimple(OK+" "+"default server");
					}else{
						writeSimple(OK+" "+ans);
					}
					break;
				case ERASE_DATE: 
					writeSimple(OK+" "+user.getEraseConditions().getDateExactCondition());
					break;
				case ERASE_FROM:
					if(user.getEraseConditions().getFrom().size()==0){
						writeSimple(OK+" "+"no conditions");
					}else{
						writeOK();
						for(String s: user.getEraseConditions().getFrom()){
							writeSimple(s);
						}
						writeEndMultiline();
					}
					break;
				case ERASE_CONTENTTYPE:
					if(user.getEraseConditions().getContentTypes().size()==0){
						writeSimple(OK+" "+"no conditions");
					}else{
						writeOK();
						for(String s: user.getEraseConditions().getContentTypes()){
							writeSimple(s);
						}
						writeEndMultiline();
					}
					break;
				case ERASE_MINSIZE: 
					writeSimple(OK+" "+user.getEraseConditions().getMinSize());
					break;
				case ERASE_MAXSIZE: 
					writeSimple(OK+" "+user.getEraseConditions().getMaxSize());
					break;
				case ERASE_ATTACHMENT:
					int i = user.getEraseConditions().getWithAttachment();
					if(i==1){
						writeSimple(OK+" "+"must have attachment");
					}else if(i==0){
						writeSimple(OK+" "+"no conditions");
					}else{
						writeSimple(OK+" "+"mustn't have attachment");
					}
					break;
				case ERASE_HEADER:
					if(user.getEraseConditions().getGeneralHeaders().size()==0){
						writeSimple(OK+" "+"no conditions");
					}else{
						writeOK();
						for(String s: user.getEraseConditions().getGeneralHeaders()){
							writeSimple(s);
						}
						writeEndMultiline();
					}
					break;
				case ERASE_PICTURE:
					i = user.getEraseConditions().getWithPicture();
					if(i==1){
						writeSimple(OK+" "+"must have pictures");
					}else if(i==0){
						writeSimple(OK+" "+"no conditions");
					}else{
						writeSimple(OK+" "+"mustn't have pictures");
					}
					break;
				case ANONYMOUS_T:
					writeSimple(user.getTransformers().contains(AnonymousTransformer.getInstance())?OK+" yes":OK+" no");
					break;
				case IMAGE_T:
					writeSimple(user.getTransformers().contains(ImageRotationTransformer.getInstance())?OK+" yes":OK+" no");
					break;
				case VOWELS_T:
					writeSimple(user.getTransformers().contains(VowelTransformer.getInstance())?OK+" yes":OK+" no");
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
