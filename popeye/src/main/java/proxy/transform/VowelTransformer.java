package proxy.transform;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import proxy.Mail;


public class VowelTransformer implements MailTransformer {

	private static VowelTransformer t;

	private VowelTransformer(){}

	public void transform(Mail mail) throws IOException {
		File file = new File("./mail"+mail.id()+"T.txt");
		file.createNewFile();
		File file2 = new File("./mail"+mail.id()+".txt");
		RandomAccessFile reader = new RandomAccessFile("./mails/mail"+mail.id()+".txt", "r");
		RandomAccessFile writer = new RandomAccessFile("./mails/mail"+mail.id()+"T.txt", "rw");
		String line;
		int i = 1;
		writer.write((reader.readLine()+"\r\n").getBytes());
		int bodyEnd = mail.getBodyEnd();
		int bodyBeg = mail.getBodyIndex();
		int htmlEnd = mail.getHtmlEnd();
		int htmlBeg= mail.getHtmlBeg();
		int aux=0;
		while((line=reader.readLine())!=null){
			if(i==bodyBeg){
				while(i<bodyEnd){
					writer.write((leet(line)+"\r\n").getBytes());
					i++;
					line=reader.readLine();
				}
			}
			if(i==htmlBeg){
				while(i<bodyEnd){
					writer.write((leetHTML(line)+"\r\n").getBytes());
					i++;
					line=reader.readLine();
				}
			}
			i++;
			writer.write((line+"\r\n").getBytes());
		}
		file2.delete();
		file.renameTo(file2);
	}

	private String leet(String line){
		char[] c = line.toCharArray();
		for(int i =0; i<c.length; i++){
			switch(c[i]){
			case 'a':
			case 'A':
				c[i]='4';
				break;
			case 'e':
			case 'E':
				c[i]='3';
				break;
			case 'i':
			case 'I':
				c[i]='1';
				break;
			case 'o':
			case 'O':
				c[i]='0';
				break;

			}
		}
		return new String(c);
	}

	private String leetHTML(String line){
		boolean inTag=false;
		char[] c = line.toCharArray();
		for(int i =0; i<c.length; i++){
			switch(c[i]){
			case '<':
				inTag=true;
				break;
			case '>':
				inTag=false;
				break;
			case 'a':
			case 'A':
				if(!inTag)
					c[i]='4';
				break;
			case 'e':
			case 'E':
				if(!inTag)
					c[i]='3';
				break;
			case 'i':
			case 'I':
				if(!inTag)
					c[i]='1';
				break;
			case 'o':
			case 'O':
				if(!inTag)
					c[i]='0';
				break;

			}
		}
		return new String(c);

	}

	public static VowelTransformer getInstance() {
		if(t==null){
			t=new VowelTransformer();
		}
		return t;
	}

	@Override
	public String toString() {
		return "Vowel Transformer";
	}

}
