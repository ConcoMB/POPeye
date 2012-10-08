package proxy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

public class aux {

	private enum State{
		AUTHORIZATION_USER, AUTHORIZATION_PASS, TRANSACTION, UPDATE;
	}
	private enum Command{
		USER, PASS, LIST, RETR, QUIT, UIDL, DELE, STAT, NOOP, RSET, APOP, TOP;
	}
	private State state; 
	private static final String OK="+OK", ERR = "-ERR", END=".", welcomeLine = "+OK POPeye at your service\n";
	private Writeable out;
	private Command lastCommand;
	private User user;

	private Map<String, User> users;
	private String userName;
	private BufferedWriter log;
	private int messageNum;


	private final static String defaultServer = "192.168.0.11";

	public void proxyServer(String line) throws IOException{

		switch(lastCommand){
		case USER:	
			if(line.startsWith(OK)){
				//users.get(user).addSuccessfulAccess();
				log.write("OK!\n");
			}else if(line.startsWith(ERR)){
				user.getStats().addAccessFailure();
			}
			out.writeToClient(line);	
		case PASS:
			if(line.startsWith(OK)){

				log.write(userName+ "logged in\n");
				state=State.TRANSACTION;
				user.addSuccessfulAccess();
			}else if(line.startsWith(ERR)){
				users.get(user).getStats().addAccessFailure();
				user=null;
			}
			out.writeToClient(line);	
			break;
		case LIST:
			log.write(" of message "+ messageNum+"\n");
			out.writeToClient(line);	
			break;
			//		case LIST_MULTI:
			//			log.write("\n");
			//			//multilined
			//			while(!resp.equals(END)){
			//				outCli.write(resp);
			//				resp = inServ.read();
			//			}
			//			break;
		case RETR:

		case DELE:

		case STAT:
		case NOOP:
		case RSET:
			out.writeToClient(line);
			break;
		case TOP:

		case UIDL:

		//case UIDL_MULTI:

		case QUIT:
			if(state==State.TRANSACTION){
				log.write(" updating data...");
				state=State.UPDATE;
			}
			out.writeToClient(line);
			log.write(" closing connections...\n");
			closeConnections();
			break;
		}
	}
	
	private void closeConnections(){
		
	}
}
