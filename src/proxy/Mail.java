package proxy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import proxy.transform.VowelMailTransformer;

public class Mail {

	private static final String FROM = "From:", DATE="Date: ", MULTIPART= "Content-Type: multipart", CONTENTTYPE="Content-Type: ",
			TEXT="Content-Type: text/plain", CTE= "Content-Transfer-Encoding: ", PIC="Content-Type: image";

	private String date ; 
//	private String subject; // "Subject: ";
	private String from;
	private Set<String> contentTypes = new HashSet<String>();
	private int bodyIndex, bodyEnd;
	private List<String> photos = new ArrayList<String>();
	private String message="";
	
	
	public Mail(){
		
	}
	
	
	public Mail(String message) {
		this.message=message;
		parse();
	}
	
	public void add(String s){
		message+=(s+"\n");
	}
	

	
	public void parse(){
		boolean flag=false;
		String[] m = message.split("\n");
		Set<String> bounds = new HashSet<String>();
		for(int i=0; i<m.length; i++){
			if(m[i].startsWith(FROM)){
				from = m[i].split("<")[1].split(">")[0];
			}else if(m[i].startsWith(DATE)){
				String[] d2 = m[i].split(" ");
				date = d2[2]+"/"+parseMonth(d2[3])+"/"+d2[4];
//			}else if(nextBound!=null && m[i].equals("--"+nextBound)){
//				bounds.add(nextBound);
//				flag=true;
			}else if(m[i].startsWith(CONTENTTYPE)){
				contentTypes.add(m[i].split(CONTENTTYPE)[1]);
				if(m[i].startsWith(MULTIPART)){
					String b;
					if(!m[i].contains("boundary")){
						i++;
					}
					b = m[i].split("boundary=")[1];
					if(b.contains("\"")){
						b=b.split("\"")[1];
					}
					bounds.add(b);
				}else if (m[i].startsWith(TEXT)){
					i++;
					if(m[i+1].startsWith(CTE)){
						i++;
					}
					bodyIndex=i;
					flag=false;
					while(!flag && i<m.length && !m[i].equals("--=20")){
						for(String b: bounds){
							if(m[i].startsWith("--"+b) || m[i].equals(b)){
								flag=true;
								break;
							}
						}
						if(!flag){
							//body+=m[i]+"\n";
							i++;
						}
					}
					bodyEnd=i;
				}else if(m[i].startsWith(PIC)){
					i++;
					while(!m[i].equals("")){
						i++;
					}
					String photo="";
					while(i<m.length && !m[i].contains("--")){
						photo+=m[i];
						i++;
					}
					//photo+=m[i];
					photos.add(photo);
				}
			}
		}
	}
	
	
	private String parseMonth(String string) {
		if(string.equals("Jan")){
			return "01";
		}
		if(string.equals("Feb")){
			return "02";
		}
		if(string.equals("Mar")){
			return "03";
		}
		if(string.equals("Apr")){
			return "04";
		}
		if(string.equals("May")){
			return "05";
		}
		if(string.equals("Jun")){
			return "06";
		}
		if(string.equals("Jul")){
			return "07";
		}
		if(string.equals("Aug")){
			return "08";
		}
		if(string.equals("Sep")){
			return "09";
		}
		if(string.equals("Oct")){
			return "10";
		}
		if(string.equals("Nov")){
			return "11";
		}
		if(string.equals("Dec")){
			return "12";
		}
		return null;
	}

	
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("./mimeExample.txt"));
		String line;
		String m="";
		while((line=br.readLine())!=null){
			m += line + '\n';
		}
		Mail mail = new Mail(m);
		VowelMailTransformer t = new VowelMailTransformer();
		t.transform(mail);
		mail.print();
	}
	
	private void print(){
		System.out.println("From: "+from);
		System.out.println("date: " + date);
		for(String s : contentTypes){
			System.out.println("ct: "+s);
		}
		System.out.println("BODY");
		printBody();
		for(String p: photos){
			System.out.println("PHOTO");
			System.out.println(p);
		}
	}

	
	public void printBody(){
		String[] s = message.split("\n");
		for(int i = bodyIndex; i<bodyEnd; i++){
			System.out.println(s[i]);
		}
	}

	public int getBodyIndex() {
		return bodyIndex;
	}
	
	public int getBodyEnd() {
		return bodyEnd;
	}
	
	public String getMessage(){
		return message;
	}


	public void setMessage(String message) {	
		this.message=message;
	}
}
