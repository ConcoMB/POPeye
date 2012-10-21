package configuration;

import java.io.IOException;
import java.text.ParseException;

import proxy.POPeye;
import user.EraseConditions;
import user.HourDenial;
import user.QuantityDenial;
import user.User;

public class Configuration {

	private POPeye proxy;
	private User user;
	
	private enum Variable{
		HOUR_MINHOUR, HOUR_MINMINUTE, HOUR_MAXHOUR, 
		HOUR_MAXMINUTE, QUANT, SERVER, ERASE_DATE, ERASE_FROM,
		ERASE_CONTENTTYPE, ERASE_MINSIZE, ERASE_MAXSIZE, ERASE_ATTACHMENT,
		ERASE_PICTURE;
	}

	public Configuration(POPeye proxy){
		this.proxy=proxy;
	}

	public void apply(String command) throws IOException{
		// check correctness of command
		String[] spl = command.split(" ");
		if(spl.length<6 || !spl[0].equals("IN") ||
				!spl[2].equals("SET") || !spl[4].equals("VALUE")){
			System.out.println("not valid config");
			return;
		}
		
		
		//TODO getuserbyname(String)
		
		// if current fetch user from proxy
		if(spl[1].equals(proxy.getCurrentUserName())){
			user = proxy.getCurrentUser();
		}
		
		
		Variable v;
		String val;
		try{
			val=spl[5].toString();
			v=Variable.valueOf(spl[3]);
		}catch(Exception e){
			System.err.println("Not valid config");
			return;
		}
		
		EraseConditions e = user.getEraseConditions();
		HourDenial h = user.getHourDenial();
		QuantityDenial q = user.getQuantityDenial();
		
		
		switch(v){
		case HOUR_MINHOUR:
			h.setMinHour(Integer.valueOf(val));
			break;
		case HOUR_MAXHOUR:
			h.setMaxHour(Integer.valueOf(val));
			break;
		case HOUR_MINMINUTE:
			h.setMinMinute(Integer.valueOf(val));
			break;
		case HOUR_MAXMINUTE:
			h.setMaxMinute(Integer.valueOf(val));
			break;
		case QUANT: 
			q.setQuantity(Integer.valueOf(val));
			break;
		case SERVER: 
			user.setServer(val);
			break;
		case ERASE_DATE: 
			try {
				e.eraseOnDate(val);
			} catch (ParseException e1) {
				System.err.println("Date format input incorrect");
			}
			break;
		case ERASE_FROM:
			try {
				e.eraseFromDate(val);
			} catch (ParseException e2) {
				System.err.println("Date format input incorrect");
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
			e.eraseAttachment(val);
			break;
		case ERASE_PICTURE:
			e.erasePicture(val);
			break;
		}

	}
}
