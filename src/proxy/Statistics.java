package proxy;

public class Statistics {

	//TODO bytes solo de los msjes?
	
	private int successfulAccesses, bytesTransferred, emailsRead, emailsErased, accessFailures, eraseFailures;

	public Statistics(){
		
	}
	
	public Statistics(String s){
		String[] split= s.split(",");
		successfulAccesses=Integer.valueOf(split[0]); 
		bytesTransferred= Integer.valueOf(split[1]);
		emailsRead=Integer.valueOf(split[2]);
		emailsErased=Integer.valueOf(split[3]);
		accessFailures=Integer.valueOf(split[4]);
		eraseFailures=Integer.valueOf(split[5]);
	}
	public String getFullStatistics() {
		return successfulAccesses +","+ bytesTransferred +","+ emailsRead+","+ emailsErased +","
				+ accessFailures +","+ eraseFailures;
	}
	
	public void addSuccessfulAccess(){
		successfulAccesses++;
	}

	public void readEmail(){
		emailsRead++;
	}
	public void eraseEmail(){
		emailsErased++;
	}

	public void addAccessFailure(){
		accessFailures++;
	}

	public void addErsaseFailure(){
		eraseFailures++;
	}

	public void addBytes(int bytes){
		bytesTransferred+=bytes;
	}

	public int getAccesses() {
		return successfulAccesses;
	}

	

}
