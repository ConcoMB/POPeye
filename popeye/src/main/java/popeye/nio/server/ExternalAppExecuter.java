package popeye.nio.server;

import java.io.*;
import java.util.*;

public class ExternalAppExecuter {
	private String app;
	
	public ExternalAppExecuter(String app){
		this.app=app;
	}
	
	public String execute(String mail) throws InterruptedException, IOException {
		List<String> command = new ArrayList<String>();
		command.add(app);
		//command.add("/A");

		ProcessBuilder builder = new ProcessBuilder(command);
		// Map<String, String> environ = builder.environment();
		//builder.directory(new File(System.getenv("temp")));

		System.out.println("Directory : " + System.getenv("temp"));
		final Process process = builder.start();
		InputStream is = process.getInputStream();
		OutputStream os = process.getOutputStream();
		System.out.println("wrote "+mail);
		InputStreamReader isr = new InputStreamReader(is);
		OutputStreamWriter osw = new OutputStreamWriter(os);
		BufferedReader br = new BufferedReader(isr);
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(mail);
		bw.write(-1);
		bw.flush();
		char c;
		StringBuffer result = new StringBuffer();
		while ((c =(char) br.read()) != -1) {
			System.out.println("char:"+c);
			result.append(c);
		}
		System.out.println("Program terminated!");
		return result.toString();
	}
}