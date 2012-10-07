package proxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class POPeye {

	private enum State{
		AUTHORIZATION, TRANSACTION, UPDATE;
	}
	private enum Command{
		USER, PASS, LIST, RETR, QUIT, UIDL, DELE, STAT, NOOP, RSET, APOP, TOP;
	}
	private State state; 
	private static final String OK="+OK", ERR = "-ERR", END=".", welcomeLine = "+OK POPeye at your service\n";


	private Map<String, User> users;
	BufferedWriter log;

	public POPeye() throws IOException{
		loadUsers();
		log = new BufferedWriter(new FileWriter("./log.txt"));
	}



	public void begin() throws IOException {
		state = State.AUTHORIZATION;
		Command c = Command.valueOf("USER");
		byte[] buff;
		InputStream inCli = new ByteArrayInputStream(buff), inServ;
		OutputStream outCli = new ByteArrayOutputStream(), outServ;
		out.write(welcomeLine.getBytes());

		boolean userAccepted=false;

		while(true){
			String read, resp, user;
			String command[] = read.split(" ");
			Command com;
			try{
				com = Command.valueOf(command[0]);
			}catch(IllegalArgumentException e){
				//ERROR
			}
			switch(com){


			case USER:
				if(state!=State.AUTHORIZATION
				|| userAccepted || command.length!=2){
					//ERROR
				}
				user=command[1];
				log.write("User "+user+" attempting to connect\n");
				if(!users.containsKey(user)){
					users.put(user, new User(user));
				}
				String serverName = users.get(user).getServer();
				if(serverName==null){
					serverName=defaultServer;
				}

				setServer(serverName);

				outServ.write(read);
				resp = inServ.read();

				if(resp.startsWith(OK)){
					//users.get(user).addSuccessfulAccess();
					userAccepted=true;
					log.write("OK!\n");
				}else if(resp.startsWith(ERR)){
					users.get(user).getStats().addAccessFailure();
				}
				outCli.write(resp);
				break;
			case PASS:
				if(state!=State.AUTHORIZATION
				|| !userAccepted || command.length!=2){
					//ERROR
				}
				log.write("Password: "+command[1]+"\n");
				outServ.write(read);
				resp = inServ.read();
				userAccepted=false;
				if(resp.startsWith(OK)){
					if(loginRestricted(user)){
						outServ.write("QUIT\n");
						resp=ERR+ "POPeye doesn't let you log in, screw you\n";
						log.write("Access blocked by POPeye\n");
						users.get(user).getStats().addAccessFailure();
					}else{
						log.write(user+ "logged in\n");
						state=State.TRANSACTION;
						users.get(user).addSuccessfulAccess();
					}
				}else if(resp.startsWith(ERR)){
					users.get(user).getStats().addAccessFailure();
				}
				outCli.write(resp);	
				break;

			case LIST:
				if(state!=State.TRANSACTION || command.length>2){
					//ERROR
				}
				log.write(user+" requested LIST");
				outServ.write(read);
				resp = inServ.read();
				if(command.length==1){
					log.write("\n");
					//multilined
					while(!resp.equals(END)){
						outCli.write(resp);
						resp = inServ.read();
					}
				}else{
					log.write(" of message "+ command[1]+"\n");
				}
				outCli.write(resp);
				break;
			case RETR:
				if(state!=State.TRANSACTION || command.length!=2){
					//ERROR
				}
				log.write(user+ " requested RETR of message "+ command[1]+"\n");
				outServ.write(read);
				resp = inServ.read();
				List<String> message = new ArrayList<String>();
				message.add(resp);
				while(!resp.equals(END)){
					message.add(inServ.read());
				}
				message.add(resp);
				log.write("Transforming message\n");
				List<String> transMessage = transform(message);
				int bytes = 0;
				for(String s: transMessage){
					outCli.write(s);
					//TODO not sure
					bytes+=s.length();
				}
				users.get(user).getStats().addBytes(bytes);
				users.get(user).getStats().readEmail();
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
				log.write(user + "requested DELE of message "+ command[1]+", checking permissions...\n");
				outServ.write("RETR "+command[1]+"\n");
				List<String> message = new ArrayList<String>();
				message.add(resp);
				while(!resp.equals(END)){
					message.add(inServ.read());
				}
				if(cantErase(message)){
					log.write("Permission to erase dennied\n");
					outCli.write(ERR+" POPeye says you can't erase that!\n");
					users.get(user).getStats().addErsaseFailure();
				}else{
					log.write("Marking message as deleted\n");
					users.get(user).getStats().eraseEmail();
					outServ.write(read);
					resp = inServ.read();
					outCli.write(resp);
				}
				break;
			case STAT:
			case NOOP:
			case RSET:

				if(state!=State.TRANSACTION || command.length!=1){
					//ERROR
				}
				log.write(user +" requested "+ c.toString()+ "\n");
				outServ.write(read);
				resp = inServ.read();
				outCli.write(resp);
				break;
			case APOP:
				//TODO
				break;
			case TOP: 
				if(state!=State.TRANSACTION || command.length!=3){
					//ERROR
				}
				log.write(user + " requested TOP of message "+ command[1]+ ", number of lines: "+ command[2]+ "\n");
				outServ.write(read);
				resp = inServ.read();
				while(!resp.equals(END)){
					outCli.write(resp);
					resp = inServ.read();
				}
				outCli.write(resp);
				break;
			case UIDL:
				if(state!=State.TRANSACTION || command.length>2){
					//ERROR
				}
				log.write(user + " requested UIDL");
				outServ.write(read);
				resp = inServ.read();
				if(command.length==1){
					log.write("\n");
					//multilined
					while(!resp.equals(END)){
						outCli.write(resp);
						resp = inServ.read();
					}
				}else{
					log.write(" of message "+ command[1]+"\n");
				}
				outCli.write(resp);
				break;
			case QUIT:
				if(command.length!=1){
					//ERROR
				}
				log.write("Quitting...");
				if(state==State.TRANSACTION){
					log.write(" updating data...");
					state=State.UPDATE;
				}
				outServ.write(read);
				resp = inServ.read();
				outCli.write(resp);
				log.write(" closing connections...\n");
				closeConnections();
				break;
			}
		}
		//TODO podria esta en un finally
		end();
	}

	private boolean loginRestricted(String user) {
		return users.get(user).accessIsBlocked();
	}



	private void end() throws IOException{
		log.close();
		saveStatistics();
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
		for(User u: users.values()){
			BufferedWriter stt = new BufferedWriter(new FileWriter("./statistics_"+u.getName()+".txt"));
			stt.write(u.getStats().getFullStatistics());
			stt.close();
		}
	}

	private void loadUsers() throws IOException{
		users=new HashMap<String, User>();
		BufferedReader u = new BufferedReader(new FileReader("./users.txt"));
		String name = u.readLine();
		while(name!=null){
			Statistics stats = loadStatistics(name);
			String server = loadServer(name);
			QuantityDenial quantityDenial= loadQuantityDenial(name);
			HourDenial hourDenial = loadHourDenial(name);
			EraseConditions eraseConds = loadEraseConditions(name);
			User user = new User(name, stats, server, quantityDenial, hourDenial, eraseConds);
			users.put(name,user);
			name = u.readLine();
		}
		u.close();
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
