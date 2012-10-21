package user;

public class User {

	private Statistics stats;
	private String name,server;
	private QuantityDenial quantityDenial;
	private HourDenial hourDenial;
	private EraseConditions eraseConditions;
	
	public User(){}
	
	public User(String name) {
		this.name=name;
		stats=new Statistics();
	}
	
	public User(String name,Statistics stats, String server, QuantityDenial quantityDenial,
			HourDenial hourDenial, EraseConditions eraseConditions) {
		this.stats = stats;
		if(stats==null){
			this.stats=new Statistics();
		}
		this.name=name;
		this.server = server;
		this.quantityDenial = quantityDenial;
		this.hourDenial = hourDenial;
		this.eraseConditions = eraseConditions;
	}
	
	
	public Statistics getStats() {
		return stats;
	}
	public String getServer() {
		return server;
	}
	public QuantityDenial getQuantityDenial() {
		return quantityDenial;
	}
	public HourDenial getHourDenial() {
		return hourDenial;
	}
	public EraseConditions getEraseConditions() {
		return eraseConditions;
	}
	
	public int hashCode(){
		return name.hashCode();
	}
	
	public boolean equals(Object o ){
		if(o==null){
			return false;
		}
		if(!(o instanceof User)){
			return false;
		}
		User u = (User)o;
		return this.name.equals(u.name);
	}

	public String getName() {
		return name;
	}

	public boolean accessIsBlocked() {
		if(hourDenial!=null && quantityDenial!=null){
			return hourDenial.isBlocked() || quantityDenial.isBlocked();
		}
		if(hourDenial!=null){
			return hourDenial.isBlocked();
		}
		if(quantityDenial!=null){
			return quantityDenial.isBlocked();
		}
		return false;
	}

	public void addSuccessfulAccess() {
		stats.addSuccessfulAccess();	
		if(quantityDenial!=null){
			quantityDenial.addAccess();
		}
	}

	// If change default to another server
	public void setServer(String val) {
		server = val;
	}
	
}
