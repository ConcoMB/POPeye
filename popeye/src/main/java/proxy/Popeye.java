package proxy;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
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
import config.Configuration;
import connection.Connection;

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
	private Connection con;
	private String mailToDelete;


	private Mail mail = new Mail();
	private int mailNum, topLines;
	//private final static String defaultServer = "pop3.alu.itba.edu.ar";
	private final static String defaultServer = "pop.aol.com";

	//private final static String defaultServer = "10.6.0.223";

	public Popeye(Writeable out, Connection con) throws IOException{
		this.con=con;
		this.out=out;
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
		Olivia.addConnection();
		userName=command[1].trim();
		user =users.get(userName);
		System.out.println("usuario:("+userName+")");
		if(user==null){
			user=new User(userName);
			users.put(userName, user);
		}

		if(user.accessIsBlocked()){
			user.getStats().addAccessFailure();
			user=null;
			return null;
		}
		String serverName = user.getServer();
		if(serverName==null){
			serverName=Configuration.getInstance().getDefaultServer();
		}
		lastCommand=Command.USER;
		state=State.AUTHORIZATION_PASS;
		return serverName;

	}

	private void unknownCommand(String line) throws IOException, InterruptedException{
		//LO DEJO PASAR
		System.out.println("Received unknown command");
		out.writeToServer(con, line);
		lastCommand=Command.UNKNOWN;
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
			System.out.println("pass");
			System.out.println(state);
			if(state!=State.AUTHORIZATION_PASS || lastCommand!=Command.USER
			|| command.length!=2){
				unknownCommand(line);
				return;
			}
			out.writeToServer(con, line);

			lastCommand=com;
			break;

		case LIST:
			if(state!=State.TRANSACTION || command.length>2){
				unknownCommand(line);
				return;
			}
			out.writeToServer(con, line);
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
				unknownCommand(line);
				return;
			}
			System.out.println("RETR");
			command[1]=command[1].trim();
			mailNum=Integer.valueOf(command[1]);
			out.writeToServer(con, line);
			lastCommand=com;
			break;
		case DELE :
			if(state!=State.TRANSACTION || command.length!=2){
				unknownCommand(line);
				return;
			}
			try{
				Integer.parseInt(command[1].trim());
			}catch(Exception e){
				unknownCommand(line);
				return;
			}
			out.writeToServer(con, "RETR "+command[1]);

			mailToDelete=command[1].trim();
			lastCommand=com;
			break;
		case STAT:
		case NOOP:
		case RSET:
		case APOP:

			if(state!=State.TRANSACTION || command.length!=1){
				unknownCommand(line);
				return;
			}
			out.writeToServer(con, line);
			lastCommand=com;
			break;
		case TOP: 
			if(state!=State.TRANSACTION || command.length!=3){
				unknownCommand(line);
				return;
			}
			out.writeToServer(con, line);		
			command[1]=command[1].trim();
			command[2]=command[2].trim();
			mailNum=Integer.valueOf(command[1]);
			topLines=Integer.valueOf(command[2]);
			lastCommand=com;
			break;
		case UIDL:
			if(state!=State.TRANSACTION || command.length>2){
				unknownCommand(line);
				return;
			}
			out.writeToServer(con, line);
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
				unknownCommand(line);
				return;
			}
			out.writeToServer(con, line);


			lastCommand=com;
			break;
		default:
			unknownCommand(line);
		}
	}



	public void proxyServer(String line) throws IOException, InterruptedException, ParseException{
		switch(lastCommand){
		case USER:	
			if(line.startsWith(OK)){
				System.out.println("OK!\n");
			}else if(line.startsWith(ERR)){
				user.getStats().addAccessFailure();
			}
			out.writeToClient(con, line);
			break;
		case PASS:
			if(line.startsWith(OK)){
				state=State.TRANSACTION;
				user.addSuccessfulAccess();
				Olivia.addSuccessfulConnection();
			}else if(line.startsWith(ERR)){
				state=State.AUTHORIZATION_USER;
				if(user!=null)
					user.getStats().addAccessFailure();
				user=null;
			}
			out.writeToClient(con, line);	
			break;
		case LIST:
			out.writeToClient(con, line);	
			break;
		case LIST_MULTI:

			if(line.equals(END)){
				lastCommand=null;
			}
			out.writeToClient(con,line);
			break;
		case RETR:
			//System.out.print("Line: "+line);
			mail.add(line);
			if(line.equals(END+"\r\n")){
				mail.parse();

				int bytes = mail.getSize();
				System.out.println("transformers:"+user.getTransformers().size());
				for(MailTransformer t: user.getTransformers()){
					System.out.println(t);
					t.transform(mail);
				}
				//TODO bytes
				Set<ExternalAppExecuter> apps=user.getApps();
				//String message=mail.getMessage();
				System.out.println("external apps:"+apps.size());
				for(ExternalAppExecuter app: apps){
					try{
						//message=app.execute(message);
						app.execute(mail);
						System.out.println(app.getPath());
					}catch(IOException e){
						//TODO
						System.out.println("app: \""+app.getPath()+"\" not found");
					}
				}
				writeMail(mail);
				//out.writeToClient(client, message);
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
					System.out.println("Permission to erase dennied\n");
					out.writeToClient(con, ERR+" POPeye says you can't erase that!\n");
					user.getStats().addErsaseFailure();
				}else{
					System.out.println("Marking mail as deleted\n");
					user.getStats().eraseEmail();
					out.writeToServer(con, "DELE "+mailToDelete+"\r\n");
				}
				lastCommand=Command.UNKNOWN;
				mail=new Mail();
			}
			break;
		case STAT:
		case NOOP:
		case RSET:
		case APOP:
			out.writeToClient(con, line);
			break;
		case TOP:
			if(line.equals(END)){
				lastCommand=null;
			}
			out.writeToClient(con, line);
			break;
		case UIDL:
			out.writeToClient(con, line);
			break;
		case UIDL_MULTI:
			if(line.equals(END)){
				lastCommand=null;
			}
			out.writeToClient(con, line);

			break;
		case QUIT:
			if(state==State.TRANSACTION){
				state=State.UPDATE;
			}
			out.writeToClient(con, line);
			//closeConnections();
			break;
		default:
		case UNKNOWN:
			out.writeToClient(con, line);
		}
	}

	private void writeMail(Mail mail2) throws IOException, InterruptedException {
		RandomAccessFile r = new RandomAccessFile("./mail0.txt", "r");
		String s;
		while((s=r.readLine())!=null){
			out.writeToClient(con, s+"\r\n");
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
