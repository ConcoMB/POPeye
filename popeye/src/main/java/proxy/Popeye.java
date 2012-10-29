package proxy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nio.server.ExternalAppExecuter;
import proxy.transform.MailTransformer;
import service.Olivia;
import user.User;

public class Popeye {

	private static Set<String> blockedIPs = new HashSet<String>();
	private static Map<String, User> users = new HashMap<String, User>();

	private enum State{
		AUTHORIZATION_USER, AUTHORIZATION_PASS, TRANSACTION, UPDATE;
	}
	private enum Command{
		USER, PASS, LIST, RETR, QUIT, UIDL, DELE, STAT, NOOP, RSET, APOP, TOP, UNKNOWN, LIST_MULTI, UIDL_MULTI;
	}
	private State state; 
	private static final String OK="+OK", ERR = "-ERR", END=".";
	private Writeable out;
	private Command lastCommand;

	//private Map<String, User> users;
	private User user;
	private String userName;
	private BufferedWriter log;
	private SocketChannel client;
	private String mailToDelete;


	private Mail mail = new Mail();
	private int mailNum, topLines;
	private final static String defaultServer = "pop3.alu.itba.edu.ar";

	//private final static String defaultServer = "pop.aol.com";

	public Popeye(Writeable out, SocketChannel client) throws IOException{
		this.client=client;
		this.out=out;
		log = new BufferedWriter(new FileWriter("./log.txt"));
		state = State.AUTHORIZATION_USER;
		lastCommand = Command.UNKNOWN;
	}

	public String login(String line) throws IOException{
		String command[] = line.split(" ");
		Command com;
		try{
			com = Command.valueOf(command[0]);
		}catch(IllegalArgumentException e){
			//decir q todo mal al cli:
			return null;
		}
		if(com!=Command.USER || state!=State.AUTHORIZATION_USER
				|| command.length!=2){
			//decir q todo mal al cli:
			return null;
		}
		log.write("User "+userName+" attempting to connect\n");
		Olivia.addConnection();
		userName=command[1].trim();
		user =users.get(userName);
		System.out.println("usuario:("+userName+")");
		if(user==null){
			user=new User(userName);
			users.put(userName, user);
		}
		
		if(user.accessIsBlocked()){
			log.write("Access blocked by POPeye\n");
			user.getStats().addAccessFailure();
			user=null;
			return null;
		}
		String serverName = user.getServer();
		if(serverName==null){
			serverName=defaultServer;
		}
		lastCommand=Command.USER;
		return serverName;

	}

	public void proxyClient(String line) throws IOException, InterruptedException {
		//out.write(welcomeLine.getBytes());
		String command[] = line.split(" ");
		Command com;
		try{
			com = Command.valueOf(command[0]);
		}catch(IllegalArgumentException e){
			com = Command.UNKNOWN;
		}
		switch(com){


		case PASS:
			if(state!=State.AUTHORIZATION_PASS || lastCommand!=Command.USER
			|| command.length!=2){
				//ERROR
			}
			log.write("Password: "+command[1]+"\n");
			out.writeToServer(client, line);

			lastCommand=com;
			break;

		case LIST:
			if(state!=State.TRANSACTION || command.length>2){
				//ERROR
			}
			log.write(userName+" requested LIST");
			out.writeToServer(client, line);
			if(command.length==2){
				command[1]=command[1].trim();
				mailNum=Integer.valueOf(command[1]);
				//lastCommand=LIST_MULTI;
			}else{
				lastCommand=com;
			}
			break;
		case RETR:
			if(state!=State.TRANSACTION || command.length!=2){
				//ERROR
			}
			command[1]=command[1].trim();
			mailNum=Integer.valueOf(command[1]);
			log.write(userName+ " requested RETR of message "+ command[1]+"\n");
			out.writeToServer(client, line);
			lastCommand=com;
			break;
		case DELE :
			if(state!=State.TRANSACTION || command.length!=2){
				//ERROR
			}
			try{
				Integer.parseInt(command[1]);
			}catch(Exception e){
				//ERROR
			}
			out.writeToServer(client, "RETR "+command[1]);
			mailToDelete=command[1].trim();
			log.write(userName + "requested DELE of mail "+ command[1]+", checking permissions...\n");
			lastCommand=com;
			break;
		case STAT:
		case NOOP:
		case RSET:

			if(state!=State.TRANSACTION || command.length!=1){
				//ERROR
			}
			log.write(userName +" requested "+ com.toString()+ "\n");
			out.writeToServer(client, line);
			lastCommand=com;
			break;
		case APOP:
			//TODO
			break;
		case TOP: 
			if(state!=State.TRANSACTION || command.length!=3){
				//ERROR
			}
			log.write(userName + " requested TOP of mail "+ command[1]+ ", number of lines: "+ command[2]+ "\n");
			out.writeToServer(client, line);		
			command[1]=command[1].trim();
			command[2]=command[2].trim();
			mailNum=Integer.valueOf(command[1]);
			topLines=Integer.valueOf(command[2]);
			lastCommand=com;
			break;
		case UIDL:
			if(state!=State.TRANSACTION || command.length>2){
				//ERROR
			}
			log.write(userName + " requested UIDL\n");
			out.writeToServer(client, line);
			if(command.length==2){
				command[1]=command[1].trim();
				mailNum=Integer.valueOf(command[1]);
				lastCommand=com;
			}else{
				lastCommand=Command.UIDL_MULTI;
			}
			break;
		case QUIT:
			if(command.length!=1){
				//ERROR
			}
			log.write("Quitting...");
			out.writeToServer(client, line);


			lastCommand=com;
			break;
		default:
			out.writeToServer(client, line);
		}
	}


	
	public void proxyServer(String line) throws IOException, InterruptedException, ParseException{
		switch(lastCommand){
		case USER:	
			if(line.startsWith(OK)){
				log.write("OK!\n");
			}else if(line.startsWith(ERR)){
				user.getStats().addAccessFailure();
			}
			out.writeToClient(client, line);
			break;
		case PASS:
			if(line.startsWith(OK)){

				log.write(userName+ "logged in\n");
				state=State.TRANSACTION;
				user.addSuccessfulAccess();
				Olivia.addSuccessfulConnection();
			}else if(line.startsWith(ERR)){
				if(user!=null)
					user.getStats().addAccessFailure();
				user=null;
			}
			out.writeToClient(client, line);	
			break;
		case LIST:
			log.write("list of mail "+ mailNum+"\n");
			out.writeToClient(client, line);	
			break;
		case LIST_MULTI:

			if(line.equals(END)){
				log.write("list\n");
				lastCommand=null;
			}
			out.writeToClient(client,line);
			break;
		case RETR:
			//System.out.print("Line: "+line);
			mail.add(line);
			if(line.equals(END+"\r\n")){
				mail.parse();
				System.out.println("parsing---");
				log.write("Transforming mail\n");
				
				int bytes = mail.getSize();
				System.out.println("transformers:"+user.getTransformers().size());
				for(MailTransformer t: user.getTransformers()){
					System.out.println(t);
					t.transform(mail);
				}
				//TODO bytes
				log.write(bytes+" bytes transferred\n");
				ExternalAppExecuter app=user.getApp();
				String message=mail.getMessage();
				if(app!=null){
					try{
						message=app.execute(message);
					}catch(IOException e){
						//TODO
						System.out.println("app: \""+app.getPath()+"\" not found");
					}
				}
				out.writeToClient(client, message);
				user.getStats().addBytes(bytes);
				user.getStats().readEmail();
				Olivia.addBytes(bytes);
				mail=new Mail();
			}
			break;
		case DELE:
			mail.add(line);
			if(line.equals(END+"\r\n")){
				mail.parse();
				if(!canErase(mail)){
					log.write("Permission to erase dennied\n");
					out.writeToClient(client, ERR+" POPeye says you can't erase that!\n");
					user.getStats().addErsaseFailure();
				}else{
					log.write("Marking mail as deleted\n");
					user.getStats().eraseEmail();
					out.writeToServer(client, "DELE "+mailToDelete+"\r\n");
				}
				lastCommand=Command.UNKNOWN;
				mail=new Mail();
			}
			break;
		case STAT:
		case NOOP:
		case RSET:
			out.writeToClient(client, line);
			break;
		case TOP:
			if(line.equals(END)){
				log.write("top con mail "+ mailNum +" cant lineas "+ topLines +"\n");
				lastCommand=null;
			}
			out.writeToClient(client, line);
			break;
		case UIDL:
			log.write("uidl\n");
			out.writeToClient(client, line);
			break;
		case UIDL_MULTI:
			if(line.equals(END)){
				log.write("uidl\n");
				lastCommand=null;
			}
			out.writeToClient(client, line);

			break;
		case QUIT:
			if(state==State.TRANSACTION){
				log.write(" updating data...");
				state=State.UPDATE;
			}
			out.writeToClient(client, line);
			log.write(" closing connections...\n");
			//closeConnections();
			break;
		default:
		case UNKNOWN:
			out.writeToClient(client, line);
		}
	}

	private boolean canErase(Mail aMail) throws ParseException {
		return user.getEraseConditions().canErase(aMail);
	
	}

	public User getCurrentUser() {
		return user;
	}

	public String getCurrentUserName(){
		return userName;
	}

	public static User getUserByName(String string) {
		return users.get(string);
	}
	
	public static void addUser(User user){
		users.put(user.getName(), user);
	}
	
	public static void blockIP(String ip){
		blockedIPs.add(ip);
		System.out.println("blocked IP:"+ip);
	}

	public static boolean isBlocked(String ip){
		return blockedIPs.contains(ip);
	}
}
