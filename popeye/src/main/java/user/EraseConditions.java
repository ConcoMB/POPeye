package user;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import proxy.Mail;

public class EraseConditions {

	private Date dateLimitFrom;

	private Date dateExactCondition;

	private Set<String> from = new HashSet<String>(), contentTypes= new HashSet<String>();

	private int minSize, maxSize;

	private int withAttachment, withPicture;

	// o Algun patron sobre cabeceras (Ejemplo: List-Id eq// <foo.example.org>)

	public Date getDateLimitFrom() {
		return dateLimitFrom;
	}

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

	public EraseConditions() {
		// DEFAULT VALUES?
	}

	public boolean canErase(Mail mail) throws ParseException {
		if (dateLimitFrom != null) {
			String s = mail.getDate();

			Date date = parseDate(s);
			if (dateLimitFrom.compareTo(date) > 0) {
				return false;
			}
		}
		if (from.size() > 0) {
			String f = mail.getFrom();
			if (from.contains(f)) {
				return false;
			}
		}
		if (contentTypes.size() > 0) {
			Set<String> s = mail.getContentTypes();
			for (String ct : contentTypes) {
				if (s.contains(ct)) {
					return false;
				}
			}
		}
		int size = mail.getSize();
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
		return true;
	}

	private Date parseDate(String val) throws ParseException {
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		return format.parse(val);
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
		contentTypes.add(val);
	}

	public void eraseAttachment(String val) {
		withAttachment = Integer.valueOf(val);
	}

	public void erasePicture(String val) {
		withPicture = Integer.valueOf(val);
	}

}
