	package nio.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import proxy.Mail;

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
	
	public void execute(Mail mail) throws InterruptedException, IOException {

		final Process process = builder.start();
		InputStream is = process.getInputStream();
		OutputStream os = process.getOutputStream();
		InputStreamReader isr = new InputStreamReader(is);
		OutputStreamWriter osw = new OutputStreamWriter(os);
		BufferedReader br = new BufferedReader(isr);
		BufferedWriter bw = new BufferedWriter(osw);
		
		File file = new File("./mails/mail"+mail.id()+"T.txt");
		file.createNewFile();
		File file2 = new File("./mails/mail"+mail.id()+".txt");
		RandomAccessFile r = new RandomAccessFile("./mails/mail"+mail.id()+".txt", "r");
		String s;
		while((s=r.readLine())!=null){
			bw.write(s);
		}
		bw.close();
		process.waitFor();
		int c;
		
		RandomAccessFile w = new RandomAccessFile("./mails/mail"+mail.id()+"T.txt", "rw");
		//StringBuffer result = new StringBuffer();
		//TODO probar esto
		while ((c = br.read()) != -1) {
			w.write((char)c);
		}
		//return result.toString();
		
		file2.delete();
		file.renameTo(file2);
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