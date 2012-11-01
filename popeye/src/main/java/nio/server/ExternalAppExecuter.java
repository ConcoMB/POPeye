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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ExternalAppExecuter))
			return false;
		ExternalAppExecuter other = (ExternalAppExecuter) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
	

}