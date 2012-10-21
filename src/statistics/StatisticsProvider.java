package statistics;

import proxy.POPeye;
import user.Statistics;
import user.User;

public class StatisticsProvider {
	
	private int bytesTransferred;
	private int successfulConnections, connections;
	private POPeye proxy;
	
	private enum Command{
		BYTES, CONNECTIONS, FULL,SUCCESSFUL_CONNECTIONS, FAILED_CONNECTIONS, EMAILS_READ, EMAILS_ERASED, ERASE_FAILURES;
	}
	
	public StatisticsProvider(POPeye proxy){
		this.proxy=proxy;
	}
	
	public void consult(String line){
		String[] command = line.split(" ");
		Command c;
		if(!command[0].equals("IN") || !command[2].equals("ASK")){
			//ERROR
			return;
		}
		try{
			c = Command.valueOf(command[3]);
		}catch(Exception e){
			//ERROR
			return;
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
			User user = proxy.getUserByName(command[1]);
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
			default:
				//EROR
				return;	
			}
			
		}
	}

	
	public void writeSimple(int info){
		//TODO
	}
	
	public void writeSimple(String info){
		//TODO
	}
	
	public void writeEndMultiline(){
		//TODO
	}
	
	public void writeFullStats(Statistics stats){
		writeSimple("Connections: " + stats.getAccesses() +"\n");
		writeSimple("Connections failed : " + stats.getAccessFailures() +"\n");
		writeSimple("Bytes transferred: " + stats.getBytesTransferred() +"\n");
		writeSimple("Emails erased: " + stats.getEmailsErased() +"\n");
		writeSimple("Emails read: " + stats.getEmailsRead() +"\n");
		writeSimple("Erase failures: " + stats.getEraseFailures() +"\n");
		writeEndMultiline();
	}
	
	public void writeFullStats(){
		writeSimple("Connections: " + connections +"\n");
		writeSimple("Connections failed : " + successfulConnections +"\n");
		writeSimple("Bytes transferred: " + bytesTransferred +"\n");
		writeEndMultiline();
	}
}
