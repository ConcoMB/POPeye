package proxy;

public interface Writeable {

	public void writeToClient(String line);
	
	public void writeToServer(String line);
	
}
