package user;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class EraseConditions {

	private Date dateLimitFrom;
	
	private Date dateExactCondition;

	private Set<String> from, contentTypes;

	private int minSize, maxSize;

	private boolean withAttachment, withPicture;

	//	   o  Algun patron sobre cabeceras (Ejemplo: List-Id eq//	      <foo.example.org>)


	public EraseConditions(){
		//DEFAULT VALUES?
	}


	public boolean canErase(String message) throws ParseException{
		if(dateLimitFrom!=null){
			String s = getHeader(message, "Date");
			Date date = parseDate(s);
			if(dateLimitFrom.compareTo(date)>0){
				return false;
			}
		}
		if(from.size()>0){
			String f = getHeader(message, "From");
			if(from.contains(f)){
				return false;
			}
		}
		if(contentTypes.size()>0){
			String s = getHeader(message, "Content-Type");
			for(String ct : contentTypes){
				if(s.contains(ct)){
					return false;
				}
			}
		}
		int size = getSize(message);
		if((minSize!=0 && size<minSize)||(maxSize!=0 && size>maxSize)){
			return false;
		}
		//TODO attachments
		return true;
	}

	private String getMonth(String string) {
		if(string.equals("Jan")){
			return "01";
		}
		if(string.equals("Feb")){
			return "02";
		}
		if(string.equals("Mar")){
			return "03";
		}
		if(string.equals("Apr")){
			return "04";
		}
		if(string.equals("May")){
			return "05";
		}
		if(string.equals("Jun")){
			return "06";
		}
		if(string.equals("Jul")){
			return "07";
		}
		if(string.equals("Aug")){
			return "08";
		}
		if(string.equals("Sep")){
			return "09";
		}
		if(string.equals("Oct")){
			return "10";
		}
		if(string.equals("Nov")){
			return "11";
		}
		if(string.equals("Dec")){
			return "12";
		}
		return null;
	}

	private int getSize(String message) {
		String[] split = message.split("\n");
		String[] split2 = split[0].split(" ");
		return Integer.valueOf(split2[1]);
	}

	private String getHeader(String message, String header) {
		String[] split = message.split("\n");
		//TODO solo el header
		for(String s: split){
			if(s.startsWith(header)){
				int i = s.indexOf(':');
				return s.substring(i+1);
				//i o i+1??
			}
		}
		return null;
	}

	private Date parseDate(String val) throws ParseException {
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String[] d = val.split(" ");
		String dateStr = d[0]+"/"+getMonth(d[1])+"/"+d[2];
		Date date = format.parse(dateStr);
		return date;
	}

	public void eraseOnDate(String val) throws ParseException {
		dateExactCondition = parseDate(val);
	}
	public void eraseFromDate(String val) throws ParseException {
		dateLimitFrom = parseDate(val);
	}

	public void eraseMinSize(String val) {
		minSize = Integer.valueOf(val);
	}
	public void eraseMaxSize(String val) {
		maxSize = Integer.valueOf(val);
	}
	
	public void eraseContentType(String val) {
		
	}

	public void eraseAttachment(String val) {
		if(val.charAt(0)==1){ // VALUE 1 = true
			withAttachment = true;
		}
		else withAttachment = false; // VALUE 0 = false
	}

	public void erasePicture(String val) {
		if(val.charAt(0)==1){ 
			withPicture = true;
		}
		else withPicture = false;
	}


}
