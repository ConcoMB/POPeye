package nio.server;

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
		builder.directory(new File(System.getenv("temp")));

		System.out.println("Directory : " + System.getenv("temp"));
		final Process process = builder.start();
		InputStream is = process.getInputStream();
		OutputStream os = process.getOutputStream();
		os.write(mail.getBytes());
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;
		StringBuffer result = new StringBuffer();
		while ((line = br.readLine()) != null) {
			System.out.println(line);
			result.append(line);
		}
		System.out.println("Program terminated!");
		return result.toString();
	}
}