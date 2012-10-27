package user;

import java.util.Calendar;
import java.util.Date;

public class QuantityDenial implements Denial{
	
	private final int top;
	private int quantity;
	private Date lastLogin;
	
	public QuantityDenial() {
		top=-1;
	}

	public void addAccess(){
		if(lastLogin==null){
			lastLogin=new Date();
		}else{
			Date today = new Date();
			if(!sameDay(today, lastLogin)){
				lastLogin=today;
				quantity=0;
			}
		}
		quantity++;
	}
	
	private boolean sameDay(Date date1, Date date2){
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
		                  cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}
	
	public boolean isBlocked(){
		if(top==-1){
			return false;
		}
		return quantity>=top;
	}

	public void setTop(int val) {
		quantity=val;		
	}
	
	public int getTop(){
		return top;
	}
	
	
}
