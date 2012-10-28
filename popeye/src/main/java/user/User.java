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
	private ExternalAppExecuter app;
	
	public User(){}
	
	public User(String name) {
		this.name=name;
		stats=new Statistics();
		hourDenial=new HourDenial();
		quantityDenial=new QuantityDenial();
		eraseConditions=new EraseConditions();
		transformers.add(ImageRotationTransformer.getInstance());
//		transformers.add(AnonymousTransformer.getInstance());
//		transformers.add(VowelTransformer.getInstance());
		//quantityDenial.setTop(1);
		//hourDenial.setMaxMinute(5);

	}
	
//	public User(String name,Statistics stats, String server, QuantityDenial quantityDenial,
//			HourDenial hourDenial, EraseConditions eraseConditions) {
//		this.stats = stats;
//		if(stats==null){
//			this.stats=new Statistics();
//		}
//		this.name=name;
//		this.server = server;
//		this.quantityDenial = quantityDenial;
//		this.hourDenial = hourDenial;
//		this.eraseConditions = eraseConditions;
//	}
	
	
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
		app=new ExternalAppExecuter(path);
	}
	
	public void unsetApp(){
		app=null;
	}
	
	public ExternalAppExecuter getApp(){
		return app;
	}
}
