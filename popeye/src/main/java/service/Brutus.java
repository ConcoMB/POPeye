package service;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.text.ParseException;

import proxy.POPeye;
import proxy.Writeable;
import proxy.transform.AnonymousTransformer;
import proxy.transform.ImageRotationTransformer;
import proxy.transform.VowelTransformer;
import user.EraseConditions;
import user.HourDenial;
import user.QuantityDenial;
import user.User;

public class Brutus {

	private final static String OK=":)", ERROR=":(";
	
	private SocketChannel channel;
	private Writeable out;
	private User user;

	enum Variable{
		MINHOUR, MAXHOUR, QUANT, SERVER, ERASE_DATE, ERASE_FROM,
		ERASE_CONTENTTYPE, ERASE_MINSIZE, ERASE_MAXSIZE, ERASE_ATTACHMENT,
		ERASE_PICTURE, BLOCK_IP, ANONYMOUS_T, VOWELS_T, IMAGE_T;
	}

	public Brutus(Writeable out, SocketChannel channel) {
		this.out = out;
		this.channel = channel;
	}

	public void apply(String command) throws IOException, InterruptedException{

		// check correctness of command
		String[] spl = command.split(" ");
		if(spl.length<6 || !spl[0].equals("IN") ||
				!spl[2].equals("SET") || !spl[4].equals("VALUE")){
			invalidConfig();
			return;
		}

		if (!spl[1].equals("GENERAL")) {
			user = POPeye.getUserByName(spl[1]);
			if(user == null){
				user = new User(spl[1]);
			}
			POPeye.addUser(user);
		}
		Variable v;
		String val;
		try{
			val=spl[5].toString();
			v=Variable.valueOf(spl[3]);
		}catch(Exception e){
			error("invalid variable");
			return;
		}

		if (user == null) {
			switch(v){
			case BLOCK_IP:
				POPeye.blockIP(val);
				break;
			default:
				//ERROR
				error("invalid variable");
				break;
			}
		}else{
			EraseConditions e = user.getEraseConditions();
			HourDenial hd = user.getHourDenial();
			QuantityDenial q = user.getQuantityDenial();
			int h, m;

			switch(v){
			case MINHOUR:
				String[] s = val.split(":");
				h = Integer.valueOf(s[0]);
				m = Integer.valueOf(s[1]);
				if(!validateHour(h) || ! validateMin(m)){
					//ERROR
					error("invalid time format");
					return;
				}else{
					hd.setMinHour(h);
					hd.setMinMinute(m);
				}
				break;
			case MAXHOUR:
				String[] s2 = val.split(":");
				h = Integer.valueOf(s2[0]);
				m = Integer.valueOf(s2[1]);
				if(!validateHour(h) || ! validateMin(m)){
					error("invalid time format");
					return;
					//ERROR
				}else{
					hd.setMaxHour(h);
					hd.setMaxMinute(h);
				}
				break;

			case QUANT: 
				q.setTop(Integer.valueOf(val));
				break;
			case SERVER: 
				user.setServer(val);
				break;
			case ERASE_DATE: 
				try {
					e.eraseOnDate(val);
				} catch (ParseException e1) {
					error("invalid date format");
					return;
				}
				break;
			case ERASE_FROM:
				try {
					e.eraseFromDate(val);
				} catch (ParseException e2) {
					error("invalid date format");
					return;
				}
				break;
			case ERASE_CONTENTTYPE:
				e.eraseContentType(val);
				break;
			case ERASE_MINSIZE: 
				e.eraseMinSize(val);
				break;
			case ERASE_MAXSIZE: 
				e.eraseMaxSize(val);
				break;
			case ERASE_ATTACHMENT:
				if(!val.equals("1") || !val.equals("0") || !val.equals("-1")){
					//EROR
					error("invalid value");
					return;
				}else{
					e.eraseAttachment(val);
				}
				break;
			case ERASE_PICTURE:
				if(!val.equals("1") || !val.equals("0") || !val.equals("-1")){
					//EROR
					error("invalid value");
					return;
				}else{
					e.erasePicture(val);
				}
				break;
			case ANONYMOUS_T:
				if(val.equals("1")){
					user.addTransformer(AnonymousTransformer.getInstance());
				}else if(val.equals("0")){
					user.removeTransformer(AnonymousTransformer.getInstance());
				}
				break;
			case IMAGE_T:
				if(val.equals("1")){
					System.out.println(user.getTransformers().size());
					user.addTransformer(ImageRotationTransformer.getInstance());
					System.out.println(user.getTransformers().size());
				}else if(val.equals("0")){
					user.removeTransformer(ImageRotationTransformer.getInstance());
				}
				break;
			case VOWELS_T:
				if(val.equals("1")){
					user.addTransformer(VowelTransformer.getInstance());
				}else if(val.equals("0")){
					user.removeTransformer(VowelTransformer.getInstance());
				}
				break;
			default:
				error("invalid variable");
				break;
			}
		}

	}

	private boolean validateMin(int i){
		if(i<0 || i>59)
			return false;
		return true;

	}

	private boolean validateHour(int i){
		if(i<0 || i>23)
			return false;
		return true;
	}

	private void invalidConfig() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		error("invalid config");
	}
	
	public void error(String message) throws IOException, InterruptedException{
		out.writeToClient(channel, ERROR+" "+message+"\r\n");
	}
}
