package service;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import proxy.POPeye;
import proxy.Writeable;
import proxy.transform.AnonymousTransformer;
import proxy.transform.ImageRotationTransformer;
import proxy.transform.VowelTransformer;
import service.Brutus.Variable;
import user.Statistics;
import user.User;

public class Olivia {

	
	//TODO esto aca?
	
	private int bytesTransferred;
	private int successfulConnections, connections;
	private SocketChannel channel;
	private Writeable out;

	private enum Command{
		BYTES, CONNECTIONS, FULL,SUCCESSFUL_CONNECTIONS, FAILED_CONNECTIONS, EMAILS_READ, EMAILS_ERASED, ERASE_FAILURES,
		CHECK_VAR;
	}

	public Olivia(Writeable out, SocketChannel channel){
		this.channel=channel;
		this.out=out;
	}


	public void consult(String line) throws IOException, InterruptedException{
		String[] command = line.split(" ");
		String ans="";
		Command c;
		Variable v = null;
		if(!command[0].equals("IN") || !command[2].equals("ASK")){
			//ERROR
			return;
		}
		try{
			c = Command.valueOf(command[3]);
		}catch(Exception e){
			try{
				v=Variable.valueOf(command[3]);
				c=Command.CHECK_VAR;
			}catch(Exception e2){
				//ERROR
				return;
			}
		}
		if(command[1].equals("GENERAL")){
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
				return;
			}
		}else{
			User user = POPeye.getUserByName(command[1]);
			if(user==null){
				//ERROR
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
				default:
					//ERROR;
					break;
				}
				break;

			default:
				//EROR
				return;	
			}

		}
	}


	public void writeSimple(int info){
		//TODO
	}

	public void writeSimple(String info) throws IOException, InterruptedException{
		out.writeToClient(channel, info+"\n");
	}

	public void writeEndMultiline(){
		//TODO
	}

	public void writeFullStats(Statistics stats) throws IOException, InterruptedException{
		writeSimple("Connections: " + stats.getAccesses() +"\n");
		writeSimple("Connections failed : " + stats.getAccessFailures() +"\n");
		writeSimple("Bytes transferred: " + stats.getBytesTransferred() +"\n");
		writeSimple("Emails erased: " + stats.getEmailsErased() +"\n");
		writeSimple("Emails read: " + stats.getEmailsRead() +"\n");
		writeSimple("Erase failures: " + stats.getEraseFailures() +"\n");
		writeEndMultiline();
	}

	public void writeFullStats() throws IOException, InterruptedException{
		writeSimple("Connections: " + connections +"\n");
		writeSimple("Connections failed : " + successfulConnections +"\n");
		writeSimple("Bytes transferred: " + bytesTransferred +"\n");
		writeEndMultiline();
	}
}
