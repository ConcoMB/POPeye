package user;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HourDenial implements Denial{

	private int minHour=-1, maxHour=-1;

	public int getMinHour() {
		return minHour;
	}

	public int getMaxHour() {
		return maxHour;
	}

	public boolean isBlocked() {
		Date now = new Date();
		DateFormat hhmmssFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
		String time = hhmmssFormat.format(now).substring(8, 13);
		String[] hhmm=time.split(":");
		int h = Integer.valueOf(hhmm[0]);
		int m = Integer.valueOf(hhmm[1]);
        //TODO arreglar esto
		System.out.println("hour:"+h);
		if(minHour!=-1 && maxHour!=-1 && minHour<maxHour){
			if((h<minHour) || (h>maxHour)){
				return true;
			}
		}else if(minHour!=-1 && maxHour!=-1 && minHour>maxHour){
			if((h<minHour) && (h>maxHour)){
				return true;
			}
		}else if(minHour==-1 && maxHour!=-1){
			if(h>maxHour){
				return true;
			}
		}else if(maxHour==-1 && minHour!=-1){
			if(h<minHour){
				return true;
			}
		}
		
		return false;
	}


	public void setMinHour(int val) {
		if(val<0 || val>23){
			return;
		}
		minHour = val;
	}

	public void setMaxHour(int val) {
		if(val<0 || val>23){
			return;
		}
		maxHour = val;
	}
}
