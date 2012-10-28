package nio.server;

import java.io.*;
import java.util.*;

public class ExternalAppExecuter {
	private ProcessBuilder builder;
	private String path;
	
	public ExternalAppExecuter(String path){
		List<String> command = new ArrayList<String>();
		command.add(path);
		this.path=path;
		//command.add("/A");

		builder = new ProcessBuilder(command);
	}
	
	public String execute(String mail) throws InterruptedException, IOException {
		// Map<String, String> environ = builder.environment();
		//builder.directory(new File(System.getenv("temp")));

		//builder.inheritIO();
		final Process process = builder.start();
		InputStream is = process.getInputStream();
		OutputStream os = process.getOutputStream();
		InputStreamReader isr = new InputStreamReader(is);
		OutputStreamWriter osw = new OutputStreamWriter(os);
		BufferedReader br = new BufferedReader(isr);
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(mail);
		bw.close();
		process.waitFor();
		int c;
		StringBuffer result = new StringBuffer();
		while ((c = br.read()) != -1) {
			result.append((char)c);
		}
		return result.toString();
	}

	public String getPath() {
		return path;
	}
}