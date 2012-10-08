package proxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SocketChannel;
import java.util.Properties;

public class POPeye {

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

	//private Map<String, User> users;
	private User user;
	private String userName;
	private BufferedWriter log;
	private SocketChannel client;

	private int messageNum, topLines;
	
	private final static String defaultServer = "192.168.0.11";

	public POPeye(Writeable out, SocketChannel client) throws IOException{
		this.client=client;
		this.out=out;
		log = new BufferedWriter(new FileWriter("./log.txt"));
		state = State.AUTHORIZATION_USER;
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
		
		if(loginRestricted(userName)){
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

	public void proxyClient(String line) throws IOException {
		Command c;
		//out.write(welcomeLine.getBytes());
		String user;
		String command[] = line.split(" ");
		Command com;
		try{
			com = Command.valueOf(command[0]);
		}catch(IllegalArgumentException e){
			return;
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
				messageNum=Integer.valueOf(command[1]);
				//lastCommand=LIST_MULTI;
			}else{
				lastCommand=com;
			}
			break;
		case RETR:
			if(state!=State.TRANSACTION || command.length!=2){
				//ERROR
			}
			messageNum=Integer.valueOf(command[1]);
			log.write(userName+ " requested RETR of message "+ command[1]+"\n");
			out.writeToServer(client, line);

			//			resp = inServ.read();
			//			List<String> message = new ArrayList<String>();
			//			message.add(resp);
			//			while(!resp.equals(END)){
			//				message.add(inServ.read());
			//			}
			//			message.add(resp);
			//			log.write("Transforming message\n");
			//			List<String> transMessage = transform(message);
			//			int bytes = 0;
			//			for(String s: transMessage){
			//				outCli.write(s);
			//				//TODO not sure
			//				bytes+=s.length();
			//			}
			//			users.get(user).getStats().addBytes(bytes);
			//			users.get(user).getStats().readEmail();
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
			messageNum=Integer.valueOf(command[1]);

			log.write(userName + "requested DELE of message "+ command[1]+", checking permissions...\n");
			out.writeToServer(client, "RETR "+command[1]+"\n");
			//			List<String> message = new ArrayList<String>();
			//			message.add(resp);
			//			while(!resp.equals(END)){
			//				message.add(inServ.read());
			//			}
			//			if(cantErase(message)){
			//				log.write("Permission to erase dennied\n");
			//				outCli.write(ERR+" POPeye says you can't erase that!\n");
			//				users.get(user).getStats().addErsaseFailure();
			//			}else{
			//				log.write("Marking message as deleted\n");
			//				users.get(user).getStats().eraseEmail();
			//				outServ.write(read);
			//				resp = inServ.read();
			//				outCli.write(resp);
			//			}
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
			log.write(userName + " requested TOP of message "+ command[1]+ ", number of lines: "+ command[2]+ "\n");
			out.writeToServer(client, line);
			//			resp = inServ.read();
			//			while(!resp.equals(END)){
			//				outCli.write(resp);
			//				resp = inServ.read();
			//			}
			//			outCli.write(resp);
			messageNum=Integer.valueOf(command[1]);
			topLines=Integer.valueOf(command[2]);
			lastCommand=com;
			break;
		case UIDL:
			if(state!=State.TRANSACTION || command.length>2){
				//ERROR
			}
			log.write(userName + " requested UIDL\n");
			out.writeToServer(client, line);

			//			resp = inServ.read();
			//			if(command.length==1){
			//				log.write("\n");
			//				//multilined
			//				while(!resp.equals(END)){
			//					outCli.write(resp);
			//					resp = inServ.read();
			//				}
			//			}else{
			//				log.write(" of message "+ command[1]+"\n");
			//			}
			//			outCli.write(resp);
			if(command.length==2){
				messageNum=Integer.valueOf(command[1]);
				lastCommand=com;
			}else{
				//lastCommand=UIDL_MULTI;
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
		}
	}
	
	public void proxyServer(String line) throws IOException{

		switch(lastCommand){
		case USER:	
			if(line.startsWith(OK)){
				//users.get(user).addSuccessfulAccess();
				log.write("OK!\n");
			}else if(line.startsWith(ERR)){
				user.getStats().addAccessFailure();
			}
			out.writeToClient(client, line);	
		case PASS:
			if(line.startsWith(OK)){

				log.write(userName+ "logged in\n");
				state=State.TRANSACTION;
				user.addSuccessfulAccess();
			}else if(line.startsWith(ERR)){
				users.get(user).getStats().addAccessFailure();
				user=null;
			}
			out.writeToClient(client, line);	
			break;
		case LIST:
			log.write(" of message "+ messageNum+"\n");
			out.writeToClient(client, line);	
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
			out.writeToClient(client, line);
			break;
		case TOP:

		case UIDL:

		//case UIDL_MULTI:

		case QUIT:
			if(state==State.TRANSACTION){
				log.write(" updating data...");
				state=State.UPDATE;
			}
			out.writeToClient(client, line);
			log.write(" closing connections...\n");
			closeConnections();
			break;
		}
	}
	
	private void closeConnections() throws IOException{
		user=null;
		saveStatistics();

	}

	private boolean loginRestricted(String user) {
		return users.get(user).accessIsBlocked();
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
//		for(User u: users.values()){
			BufferedWriter stt = new BufferedWriter(new FileWriter("./statistics_"+userName+".txt"));
			stt.write(user.getStats().getFullStatistics());
			stt.close();
//		}
	}

//	private void loadUsers() throws IOException{
//		users=new HashMap<String, User>();
//		BufferedReader u = new BufferedReader(new FileReader("./users.txt"));
//		String name = u.readLine();
//		while(name!=null){
//			Statistics stats = loadStatistics(name);
//			String server = loadServer(name);
//			QuantityDenial quantityDenial= loadQuantityDenial(name);
//			HourDenial hourDenial = loadHourDenial(name);
//			EraseConditions eraseConds=null; //= loadEraseConditions(name);
//			User user = new User(name, stats, server, quantityDenial, hourDenial, eraseConds);
//			users.put(name,user);
//			name = u.readLine();
//		}
//		u.close();
//	}
	
	private User loadUser() throws IOException{
		Statistics stats = loadStatistics(userName);
		String server = loadServer(userName);
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
}
