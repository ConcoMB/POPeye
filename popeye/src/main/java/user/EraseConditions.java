package user;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import proxy.Mail;

public class EraseConditions {

	private Date dateExactCondition;

	private Set<String> from = new HashSet<String>(), contentTypes= new HashSet<String>(), generalHeaders = new HashSet<String>();

	private int minSize, maxSize;

	private int withAttachment, withPicture;

	// o Algun patron sobre cabeceras (Ejemplo: List-Id eq// <foo.example.org>)

	public Date getDateExactCondition() {
		return dateExactCondition;
	}

	public Set<String> getFrom() {
		return from;
	}

	public Set<String> getContentTypes() {
		return contentTypes;
	}

	public int getMinSize() {
		return minSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public int getWithAttachment() {
		return withAttachment;
	}

	public int getWithPicture() {
		return withPicture;
	}

	public Set<String> getGeneralHeaders(){
		return generalHeaders;
	}

	public EraseConditions() {
		// DEFAULT VALUES?
	}

	public boolean canErase(Mail mail) throws ParseException {
		if (dateExactCondition != null) {
			String s = mail.getDate();
			Date date = parseDate(s);
			if (dateExactCondition.compareTo(date) > 0) {
				return false;
			}
		}
		if (from.size() > 0) {
			String f = mail.getFrom();
			f = f.substring(f.indexOf('<')+1, f.indexOf('>'));
			System.out.println(f+"\n");
			if (from.contains(f)) {
				return false;
			}
		}
		if (contentTypes.size() > 0) {
			Set<String> full = mail.getContentTypes();
			Set<String> s = new HashSet<String>();
			for(String ct: full){
				String reduced = ct.substring(0,ct.indexOf(';'));
				s.add(reduced);
			}
			for (String ct : contentTypes) {
				if (s.contains(ct)) {
					return false;
				}
			}
		}
		int size = mail.getSize();
		System.out.println("size:"+size+" minsize:"+minSize+" maxSize:"+maxSize);
		if ((minSize != 0 && size < minSize)
				|| (maxSize != 0 && size > maxSize)) {
			return false;
		}
		System.out.println(withAttachment+":"+(withAttachment==1));
		for(String s:mail.getContentDispositions()){
			System.out.println(s);
		}
		if (withAttachment == 1
				&& mail.getContentDispositions().contains("attachment")) {
			return false;
		} else if (withAttachment == -1
				&& !mail.getContentDispositions().contains("attachment")) {
			return false;
		}
		if (withPicture == 1 && mail.getImages().size() == 0) {
			return false;
		} else if (withPicture == -1 && mail.getImages().size() != 0) {
			return false;
		}

		for(String header: generalHeaders){
			try{
				if(mail.containsHeader(header)){
					return false;
				}
			}catch(IOException e){

			}
		}
		return true;
	}

	private Date parseDate(String val) throws ParseException {
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		return format.parse(val);
	}

	public void eraseOnDate(String val) throws ParseException {
		dateExactCondition = parseDate(val);
		System.out.println(dateExactCondition);
	}

	public void eraseFrom(String val) {
		from.add(val);
	}

	public void eraseMinSize(String val) {
		minSize = Integer.valueOf(val);
	}

	public void eraseMaxSize(String val) {
		maxSize = Integer.valueOf(val);
	}

	public void eraseContentType(String val) {
		contentTypes.add(val);
	}

	public void eraseAttachment(String val) {
		withAttachment = Integer.valueOf(val);
	}

	public void erasePicture(String val) {
		withPicture = Integer.valueOf(val);
	}

	public void addHeader(String header){
		generalHeaders.add(header);
	}
}

