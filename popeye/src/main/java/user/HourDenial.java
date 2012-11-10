package user;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HourDenial implements Denial{

	private int minHour=-1, minMinute=-1, maxHour=-1, maxMinute=-1;

	public int getMinHour() {
		return minHour;
	}

	public int getMinMinute() {
		return minMinute;
	}

	public int getMaxHour() {
		return maxHour;
	}

	public int getMaxMinute() {
		return maxMinute;
	}

	public boolean isBlocked() {
		Date now = new Date();
		DateFormat hhmmssFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
		String time = hhmmssFormat.format(now).substring(8, 13);
		String[] hhmm=time.split(":");
		int h = Integer.valueOf(hhmm[0]);
		int m = Integer.valueOf(hhmm[1]);
        //TODO arreglar esto
		if(minHour!=-1 && maxHour!=-1 && minHour<maxHour){
			if((h<minHour) || (h>maxHour)){
				return true;
			}
		}else if(minHour!=-1 && maxHour!=-1 && minHour>maxHour){
			if((h<minHour) && (h>maxHour)){
				return true;
			}
		}else if(minHour==-1){
			if(h>maxHour){
				return true;
			}
		}else if(maxHour==-1){
			if(h<minHour){
				return true;
			}
		}
//		if((minMinute!=-1 && m<minMinute) || (maxMinute!=-1 && m>maxMinute)){
//			return true;
//		}
		
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

	public void setMaxMinute(int val) {
		if(val<0 || val>59){
			return;
		}
		maxMinute = val;
	}

	public void setMinMinute(int val) {
		if(val<0 || val>59){
			return;
		}
		minMinute = val;
	}

}
