package user;

import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageTranscoder;

import nio.server.ExternalAppExecuter;

import proxy.transform.AnonymousTransformer;
import proxy.transform.ImageRotationTransformer;
import proxy.transform.MailTransformer;
import proxy.transform.VowelTransformer;

public class User {

	private Statistics stats;
	private String name,server;
	private QuantityDenial quantityDenial;
	private HourDenial hourDenial;
	private EraseConditions eraseConditions;
	private Set<MailTransformer> transformers = new HashSet<MailTransformer>();
	private Set<ExternalAppExecuter> apps = new HashSet<ExternalAppExecuter>();
	
	public User(){}
	
	public User(String name) {
		this.name=name;
		stats=new Statistics();
		hourDenial=new HourDenial();
		quantityDenial=new QuantityDenial();
		eraseConditions=new EraseConditions();
		hourDenial.setMinHour(11);
		//transformers.add(ImageRotationTransformer.getInstance());
		transformers.add(AnonymousTransformer.getInstance());
		transformers.add(VowelTransformer.getInstance());
		//quantityDenial.setTop(1);
		//hourDenial.setMaxMinute(5);
//		apps.add(new ExternalAppExecuter("/Users/Conco/popeye/popeye/apps/toUpper.o"));
//		apps.add(new ExternalAppExecuter("/Users/Conco/popeye/popeye/apps/echo.o"));
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
		return hourDenial.isBlocked() || quantityDenial.isBlocked();
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

	public void addTransformer(MailTransformer t) {
		transformers.add(t);
	}
	
	public Set<MailTransformer> getTransformers(){
		return transformers;
	}

	public void removeTransformer(MailTransformer t) {
		transformers.remove(t);
	}
	
	public void setApp(String path){
		apps.add(new ExternalAppExecuter(path));
	}
	
	public void unsetApp(String path){
		apps.remove(new ExternalAppExecuter(path));
	}
	
	public Set<ExternalAppExecuter> getApps(){
		return apps;
	}
}
