package user;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class EraseConditions {

	private Date dateLimit;

	private Set<String> from, contentTypes;

	private int minSize, maxSize;

	private int withAttachment, withPicture;

	//	   o  Algun patron sobre cabeceras (Ejemplo: List-Id eq//	      <foo.example.org>)


	public EraseConditions(){

	}

	public EraseConditions(String s) throws ParseException{
		String[] split = s.split("\n");
		if(split.length!=5){
			throw new IllegalStateException();
		}
		if(!split[0].equals("")){
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			dateLimit=format.parse(split[0]);
		}
		String[] froms = split[1].split(",");
		from=new HashSet<String>();
		for(String user: froms){
			from.add(user);
		}
		contentTypes=new HashSet<String>();
		String[] cts = split[2].split(",");
		for(String ct: cts){
			contentTypes.add(ct);
		}
		if(!split[3].equals("")){
			String[] sizes = split[3].split(",");
			minSize= Integer.valueOf(sizes[0]);
			maxSize= Integer.valueOf(sizes[1]);
		}
		String[] contents = split[4].split(",");
		if(contents[0].equals("with")){
			withAttachment=1;
		}else if(contents[0].equals("witout")){
			withAttachment=-1;
		}else if(contents[0].equals("no")){
			withAttachment=0;
		}
		if(contents[1].equals("with")){
			withPicture=1;
		}else if(contents[1].equals("witout")){
			withPicture=-1;
		}else if(contents[1].equals("no")){
			withPicture=0;
		}
	}

	public boolean canErase(String message) throws ParseException{
		if(dateLimit!=null){
			String s = getHeader(message, "Date");
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			String[] d = s.split(" ");
			String dateStr = d[0]+"/"+getMonth(d[1])+"/"+d[2];
			Date date = format.parse(dateStr);
			if(dateLimit.compareTo(date)>0){
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


}
