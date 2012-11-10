package service;

import java.io.IOException;
import java.text.ParseException;

import proxy.Popeye;
import proxy.Writeable;
import proxy.transform.AnonymousTransformer;
import proxy.transform.ImageRotationTransformer;
import proxy.transform.VowelTransformer;
import user.EraseConditions;
import user.HourDenial;
import user.QuantityDenial;
import user.User;
import connection.Connection;

public class Brutus extends Service {
	
	enum BrutusVariable {
		MINHOUR, MAXHOUR, QUANT, SERVER, ERASE_DATE, ERASE_FROM, ERASE_CONTENTTYPE, ERASE_MINSIZE, ERASE_MAXSIZE, ERASE_ATTACHMENT, ERASE_HEADER, ERASE_PICTURE, BLOCK_IP, ANONYMOUS_T, VOWELS_T, IMAGE_T, ADD_APP, DEL_APP;
	}

	public Brutus(Writeable out, Connection con) throws IOException {
		super(out, con);
	}

	public void apply(String command) throws IOException, InterruptedException {
		User user=null;
		// check correctness of command
		if (!handleConnection(command)) {
			return;
		}
		String[] spl = command.split(" ",6);
		if (spl.length == 1) {
			if (command.equals("QUIT")) {
				byebye();
				return;
			}
		}
		if (spl.length != 6 || !spl[0].equals("IN") || !spl[2].equals("SET")
				|| !spl[4].equals("VALUE")) {
			invalidConfig();
			return;
		}

		if (!spl[1].equals("GENER@L")) {
			user = Popeye.getUserByName(spl[1]);
			if (user == null) {
				user = new User(spl[1]);
			}
			Popeye.addUser(user);
		}
		BrutusVariable v;
		String val;
		try {
			val = spl[5];
			v = BrutusVariable.valueOf(spl[3]);
		} catch (Exception e) {
			invalidConfig();
			return;
		}

		if (user == null) {
			switch (v) {
			case BLOCK_IP:
				Popeye.blockIP(val);
				break;
			default:
				// ERROR
				invalidConfig();
				return;
			}
		} else {
			EraseConditions e = user.getEraseConditions();
			HourDenial hd = user.getHourDenial();
			QuantityDenial q = user.getQuantityDenial();
			int h, m;

			switch (v) {
			case MINHOUR:
				h = Integer.valueOf(val);
				if (!validateHour(h)) {
					// ERROR
					invalidConfig();
					return;
				} else {
					hd.setMinHour(h);
				}
				break;
			case MAXHOUR:
				h = Integer.valueOf(val);
				if (!validateHour(h) ) {
					invalidConfig();
					return;
				} else {
					hd.setMaxHour(h);
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
					invalidConfig();
					return;
				}
				break;
			case ERASE_FROM:
				e.eraseFrom(val);
				break;
			case ERASE_HEADER:
				e.addHeader(val);
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
				if (!val.equals("1") && !val.equals("0") && !val.equals("-1")) {
					// EROR
					invalidConfig();
					return;
				} else {
					e.eraseAttachment(val);
				}
				break;
			case ERASE_PICTURE:
				if (!val.equals("1") && !val.equals("0") && !val.equals("-1")) {
					// EROR
					invalidConfig();
					return;
				} else {
					e.erasePicture(val);
				}
				break;
			case ANONYMOUS_T:
				if (val.equals("1")) {
					user.addTransformer(AnonymousTransformer.getInstance());
				} else if (val.equals("0")) {
					user.removeTransformer(AnonymousTransformer.getInstance());
				}
				break;
			case IMAGE_T:
				if (val.equals("1")) {
					System.out.println(user.getTransformers().size());
					user.addTransformer(ImageRotationTransformer.getInstance());
					System.out.println(user.getTransformers().size());
				} else if (val.equals("0")) {
					user.removeTransformer(ImageRotationTransformer
							.getInstance());
				}
				break;
			case VOWELS_T:
				if (val.equals("1")) {
					user.addTransformer(VowelTransformer.getInstance());
				} else if (val.equals("0")) {
					user.removeTransformer(VowelTransformer.getInstance());
				}
				break;
			case ADD_APP:
				user.setApp(val);
				break;
			case DEL_APP:
				user.unsetApp(val);
				break;
			default:
				// ERROR
				invalidConfig();
				return;
			}
		}
		writeOK();
	}

	private boolean validateMin(int i) {
		if (i < 0 || i > 59)
			return false;
		return true;

	}

	private boolean validateHour(int i) {
		if (i < 0 || i > 23)
			return false;
		return true;
	}

}
