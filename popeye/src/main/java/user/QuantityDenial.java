package user;

import java.util.Calendar;
import java.util.Date;

public class QuantityDenial implements Denial{
	
	private final int top;
	private int quantity;
	private Date lastLogin;
	
	public QuantityDenial(int top){
		this.top=top;
		quantity=0;
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
		return quantity>=top;
	}

	public void setQuantity(int val) {
		quantity=val;		
	}
	
	
}
