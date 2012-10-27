package user;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HourDenial implements Denial{

	private int minHour, minMinute, maxHour, maxMinute;
	
	private void init(int minHour, int minMinute, int maxHour, int maxMinute) {
		this.minHour = minHour;
		this.minMinute = minMinute;
		this.maxHour = maxHour;
		this.maxMinute = maxMinute;
	}

	public boolean isBlocked() {
		Date now = new Date();
		DateFormat hhmmssFormat = new SimpleDateFormat("yyyyMMddhh:mm:ss");
		String time = hhmmssFormat.format(now).substring(8, 13);
		String[] hhmm=time.split(":");
		int h = Integer.valueOf(hhmm[0]);
		int m = Integer.valueOf(hhmm[1]);

		if(h<minHour || h>maxHour){
			return false;
		}
		if(m<minMinute || m>maxMinute){
			return false;
		}
		
		return true;
	}



	public void setMinHour(int val) {
		minHour = val;
	}

	public void setMaxHour(int val) {
		maxHour = val;
	}

	public void setMaxMinute(int val) {
		maxMinute = val;
	}

	public void setMinMinute(int val) {
		minMinute = val;
	}

}
