package configuration;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.text.ParseException;

import proxy.POPeye;
import proxy.Writeable;
import user.EraseConditions;
import user.HourDenial;
import user.QuantityDenial;
import user.User;

public class Configuration {

	private SocketChannel channel;
	private Writeable out;
	private User user;

	private enum Variable {
		HOUR_MINHOUR, HOUR_MINMINUTE, HOUR_MAXHOUR, HOUR_MAXMINUTE, QUANT, SERVER, ERASE_DATE, ERASE_FROM, ERASE_CONTENTTYPE, ERASE_MINSIZE, ERASE_MAXSIZE, ERASE_ATTACHMENT, ERASE_PICTURE, BLOCK_IP;
	}

	public Configuration(Writeable out, SocketChannel channel) {
		this.out = out;
		this.channel = channel;
	}

	public void apply(String command) throws IOException, InterruptedException {

		// check correctness of command
		String[] spl = command.split(" ");
		if (spl.length < 6 || !spl[0].equals("IN") || !spl[2].equals("SET")
				|| !spl[4].equals("VALUE")) {

			invalidConfig();
			return;
		}

		// TODO getuserbyname(String)

		// if current fetch user from proxy
		/*
		 * if(spl[1].equals(proxy.getCurrentUserName())){ user =
		 * proxy.getCurrentUser(); }
		 */
		if (!spl[1].equals("GENERAL")) {
			user = POPeye.getUserByName(spl[1]);
			if (user == null) {
				POPeye.addUser(spl[1]);
				user = POPeye.getUserByName(spl[1]);
			}
		}

		Variable v;
		String val;
		try {
			val = spl[5].toString();
			v = Variable.valueOf(spl[3]);
		} catch (Exception e) {
			invalidConfig();
			return;
		}

		if (user == null) {
			switch(v){
			case BLOCK_IP:
				POPeye.blockIP(val);
				break;
			}
		} else {

			EraseConditions e = user.getEraseConditions();
			HourDenial h = user.getHourDenial();
			QuantityDenial q = user.getQuantityDenial();

			switch (v) {
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

	private void invalidConfig() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		out.writeToClient(channel, "INVALID CONFIG\n");
		System.out.println("not valid config");
	}
}
