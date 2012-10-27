package popeye.proxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import popeye.user.EraseConditions;
import popeye.user.HourDenial;
import popeye.user.QuantityDenial;
import popeye.user.Statistics;
import popeye.user.User;

public class POPeye {

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


	private Mail mail = new Mail();
	private int mailNum, topLines;
	
	private final static String defaultServer = "pop3.alu.itba.edu.ar";

	public POPeye(Writeable out, SocketChannel client) throws IOException{
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
		userName=command[1];
		log.write("User "+userName+" attempting to connect\n");
		//		if(users.containsKey(userName)){
		//			user=users.get(userName);
		//		}else{
		user =loadUser();
		//			users.put(userName, user);
		//		}

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
		String user;
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
			out.writeToServer(client, "RETR "+command[1]+"\r\n");			
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


	
	public void proxyServer(String line) throws IOException, InterruptedException{
		switch(lastCommand){
		case USER:	
			if(line.startsWith(OK)){
				//users.get(user).addSuccessfulAccess();
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
			}else if(line.startsWith(ERR)){
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
			mail.add(line);
			if(line.equals(END)){
				mail.parse();
				log.write("Transforming mail\n");
				//List<String> transmail = transform(mail);
				int bytes = 0;
//				for(String s: transformMail){
//					out.writeToClient(client, line);
//					//TODO not sure
//					bytes+=s.length();
//				}
				out.writeToClient(client, mail.toString());
				users.get(user).getStats().addBytes(bytes);
				users.get(user).getStats().readEmail();
				mail=new Mail();
			}
			break;
		case DELE:
			mail.add(line);
			if(line.equals(END)){
				mail.parse();
				if(cantErase(mail)){
					log.write("Permission to erase dennied\n");
					out.writeToClient(client, ERR+" POPeye says you can't erase that!\n");
					users.get(user).getStats().addErsaseFailure();
				}else{
					log.write("Marking mail as deleted\n");
					users.get(user).getStats().eraseEmail();
					out.writeToServer(client, line);
					lastCommand=Command.DELE;
				}
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
				log.write("top\n");
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
			closeConnections();
			break;
		default:
		case UNKNOWN:
			out.writeToClient(client, line);
		}
	}

	private boolean cantErase(Mail aMail) {
//		EraseConditions er = user.getEraseConditions();
//		if(aMail.getDate().compareTo(er.getDateLimit())<0){
//			return true;
//		}
//		if(er.getFrom().contains(aMail.getFrom())){
//			return true;
//		}
//		for(String c: aMail.getContentTypes()){
//			if(er.getContentTypes.contains(c)){
//				return true;
//			}
//		}
//		if(aMail.getSize()<er.getMinSize() || aMail.getSize()>er.getMaxSize()){
//			return true;
//		}
//		if((aMail.hasAttachment() && er.getAttachment()==-1) || (!aMail.hasAttachment && er.getAttachment()==1){
//			return true;
//		}
//		//TODO picture;
		return false;
	}

	private void closeConnections() throws IOException{
		saveStatistics();
		user=null;
		//TODO
	}




	private void end() throws IOException{
		log.close();
	}

	private String loadServer(String user) throws IOException{
		Properties properties=new Properties();
		try {
			InputStream is= getClass().getClassLoader().getResourceAsStream("./servers.properties");
			properties.load(is);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new IOException();
		}
		return properties.getProperty(user);
	}

	private Statistics loadStatistics(String name) throws IOException {
		BufferedReader stt;
		try{
			stt = new BufferedReader(new FileReader("./statistics_"+name+".txt"));
		}catch(FileNotFoundException e){
			return null;
		}
		Statistics s=  new Statistics(stt.readLine());
		stt.close();
		return s;
	}

	private void saveStatistics() throws IOException{
		BufferedWriter stt = new BufferedWriter(new FileWriter("./statistics_"+userName+".txt"));
		stt.write(user.getStats().getFullStatistics());
		stt.close();
	}

	private User loadUser() throws IOException{
		Statistics stats = loadStatistics(userName);
		//String server = loadServer(userName);
		String server=null;
		QuantityDenial quantityDenial= loadQuantityDenial(userName);
		HourDenial hourDenial = loadHourDenial(userName);
		EraseConditions eraseConds=null; //= loadEraseConditions(userName);
		return new User(userName, stats, server, quantityDenial, hourDenial, eraseConds);
	}



	private HourDenial loadHourDenial(String name) throws IOException {
		BufferedReader b;
		try{
			b = new BufferedReader(new FileReader("./hourDenial_"+name+".txt"));
		}catch(FileNotFoundException e){
			return null;
		}
		HourDenial d=  new HourDenial(b.readLine());
		b.close();
		return d;
	}



	private QuantityDenial loadQuantityDenial(String name) throws NumberFormatException, IOException {
		BufferedReader b;
		try{
			b = new BufferedReader(new FileReader("./quantityDenial_"+name+".txt"));
		}catch(FileNotFoundException e){
			return null;
		}
		QuantityDenial d=  new QuantityDenial(Integer.valueOf(b.readLine()));
		b.close();
		return d;
	}

	public User getCurrentUser() {
		return user;
	}

	public String getCurrentUserName(){
		return userName;
	}

	public User getUserByName(String string) {
		return users.get(string);
	}
}
