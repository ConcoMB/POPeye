package proxy.transform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import proxy.Mail;

public class AnonymousTransformer implements MailTransformer {
	
	private static AnonymousTransformer t;
	
	private AnonymousTransformer(){}
	
	public void transform(Mail mail) throws IOException {		
		int index = mail.getFromLine();
		File file = new File("./mails/mail"+mail.id()+"T.txt");
		file.createNewFile();
		File file2 = new File("./mails/mail"+mail.id()+".txt");

		RandomAccessFile reader = new RandomAccessFile("./mails/mail"+mail.id()+".txt", "r");
		RandomAccessFile writer = new RandomAccessFile("./mails/mail"+mail.id()+"T.txt", "rw");
		String line;
		int i = 0;
		while((line=reader.readLine())!=null){
			if(i==index){
				writer.write(("From: Popeye's spinach <guess@who.com>\r\n").getBytes());
			}else{
				writer.write((line+"\r\n").getBytes());
			}
			i++;
		}
		file2.delete();
		file.renameTo(file2);
	}

	public static MailTransformer getInstance() {
		if(t==null){
			t=new AnonymousTransformer();
		}
		return t;
	}

	
	@Override
	public String toString() {
		return "Anonymous Transformer";
	}
}
