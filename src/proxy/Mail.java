package proxy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Mail {

	private static final String FROM = "From:", DATE="Date: ", MULTIPART= "ContentType: multipart", CONTENTTYPE="ContentType: ",
			TEXT="ContentType: text/plain", CTE= "Content-Transfer-Encoding: ";

	private String date ; // "Date: ";
//	private String subject; // "Subject: ";
	private String from; // "From: ";
	private Set<String> contentTypes = new HashSet<String>();
	private String body="";
//	private List<Multipart> multiparts;
	
	public Mail(String message) {
//		multiparts = new ArrayList<Multipart>();
		parse(message);
	}
	
	public void add(String s){
		//if(s.startsWith("Date: "))
	}
	
	
	private void parse(String message){
		String[] m = message.split("\n");
		Deque<String> bounds = new LinkedList<String>();
		for(int i=0; i<m.length; i++){
			if(m[i].startsWith(FROM)){
				from = m[i].split("<")[1].split(">")[0];
			}else if(m[i].startsWith(DATE)){
				String d = m[i].split(DATE)[0];
				String[] d2 = d.split(" ");
				date = d2[1]+"/"+parseMonth(d2[2])+"/"+d2[3];
			}else if(m[i].startsWith(CONTENTTYPE)){
				contentTypes.add(m[i].split(CONTENTTYPE)[0]);
				if(m[1].startsWith(MULTIPART)){
					bounds.push(m[i].split("boundary=")[1]);
				}else if (m[1].startsWith(TEXT)){
					if(m[i+1].startsWith(CTE)){
						i++;
					}
					while(i<m.length || (!bounds.isEmpty() && !m[1].equals("--"+bounds.peek()))){
						body+=m[i]+"\n";
						i++;
					}
				}
			}
			if(!bounds.isEmpty() && m[i].equals("--"+bounds.peek())){
				bounds.pop();
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
		BufferedReader br = new BufferedReader(new FileReader("./mail.example"));
		String line;
		String m="";
		while((line=br.readLine())!=null){
			m += line + '\n';
		}
		Mail mail = new Mail(m);
		mail.print();
	}
	
	private void print(){
		System.out.println("From: "+from);
		System.out.println("date: " + date);
		for(String s : contentTypes){
			
		}
	}
}
