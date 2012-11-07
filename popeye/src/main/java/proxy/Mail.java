package proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import proxy.transform.AnonymousTransformer;
import proxy.transform.VowelTransformer;

public class Mail {

	public static void main(String[] args) throws IOException {
		BufferedReader b = new BufferedReader(new FileReader("./mailExample.txt"));
		Mail m = new Mail();
		String line;

		while((line=b.readLine())!=null){
			m.add(line);
		}
		m.parse();
		System.out.println(m.fromLine+" "+ m.date+" "+m.bodyEnd + " "+m.bodyIndex+ " "+m.from);
		for(MailImage image: m.photos){
			System.out.println(image.startLine);
		}
		AnonymousTransformer.getInstance().transform(m);
		VowelTransformer.getInstance().transform(m);
	}


	private static final String FROM = "From:", DATE="Date: ", MULTIPART= "Content-Type: multipart", CONTENTTYPE="Content-Type: ",
			TEXT="Content-Type: text/plain", CTE= "Content-Transfer-Encoding: ", PIC="Content-Type: image", 
			CONTENTDISP="Content-Disposition: ", HTML="Content-Type: text/html";

	private String date ; 
	//	private String subject; // "Subject: ";
	private String from;
	private int fromLine;
	private Set<String> contentTypes = new HashSet<String>(), contentDispositions = new HashSet<String>();
	private int bodyIndex, bodyEnd, htmlBeg, htmlEnd;
	private List<MailImage> photos = new ArrayList<MailImage>();
	private RandomAccessFile reader, writer;
	private int size;

	public Mail() throws IOException {
		File f = new File("./mail0.txt");
		f.createNewFile();
		reader = new RandomAccessFile("./mail0.txt", "r");
		writer = new RandomAccessFile("./mail0.txt", "rw");
	}

	public void add(String line) throws IOException{
		size+=line.length();
		writer.write((line+"\r\n").getBytes());
	}

	public int getSize() {
		return size;
	}

	public void parse() throws IOException{
		System.out.println("Parsing mail...");
		boolean flag=false;
		Set<String> bounds = new HashSet<String>();
		int i = 0;
		writer.close();
		String line;
		while((line=reader.readLine())!=null){
			if(line.startsWith(FROM)){
				//while(!line.contains("<")){i++;}
				//from = line.split("<")[1].split(">")[0];
				fromLine=i;
				from=line.split(FROM)[1];
			}else if(line.startsWith(DATE)){
				String[] d2 = line.split(" ");
				date = d2[2]+"/"+parseMonth(d2[3])+"/"+d2[4];
				//			}else if(nextBound!=null && line.equals("--"+nextBound)){
				//				bounds.add(nextBound);
				//				flag=true;
			}else if(line.startsWith(CONTENTTYPE)){
				contentTypes.add(line.split(CONTENTTYPE)[1]);
				if(line.startsWith(MULTIPART)){
					String b;
					while(line!=null && !line.contains("boundary")){
						i++;
						line=reader.readLine();
					}
					b = line.split("boundary=")[1];
					if(b.contains("\"")){
						b=b.split("\"")[1];
					}
					bounds.add(b);
				}else if (line.startsWith(TEXT)){
					while(line!=null & !line.equals("")){
						i++;
						line=reader.readLine();
					}
					bodyIndex=++i;
					line=reader.readLine();
					flag=false;
					while(!flag && line!=null && !line.equals("--=20")){
						for(String b: bounds){
							if(line.startsWith("--"+b) || line.equals(b)){
								flag=true;
								break;
							}
						}
						if(!flag){
							//body+=line+"\n";
							line=reader.readLine();
							i++;
						}
					}
					bodyEnd=i;
				}else if(line.startsWith(PIC)){
					i++;
					line=reader.readLine();
					while(line!=null && !line.equals("")){
						i++;
						line=reader.readLine();
					}
					i++;
					line=reader.readLine();
					MailImage image = new MailImage();
					image.startLine=i;
					//String photo="";
					while(line!=null && !line.contains("--") && !line.equals("")){
						//photo+=line;
						i++;
						line=reader.readLine();
					}
					image.endLine=i-1;
					//photo+=line;
					photos.add(image);
				}else if(line.startsWith(HTML)){
					while(line!=null && !line.equals("")){
						i++;
						line=reader.readLine();

					}
					htmlBeg=i++;
					flag=false;
					while(!flag && line!=null && !line.equals("--=20")){
						for(String b: bounds){
							if(line.startsWith("--"+b) || line.equals(b)){
								flag=true;
								break;
							}
						}
						if(!flag){
							//body+=line+"\n";
							line=reader.readLine();
							i++;
						}
					}
					htmlEnd=i;
				}else{
					boolean cd=false;
					String line2=reader.readLine();
					String line3=reader.readLine();

					if(line2.startsWith(CONTENTDISP)){
						i++;
						cd=true;
						line=line2;
					}else if(line3.startsWith(CONTENTDISP)){
						i+=2;
						cd=true;
						line=line3;
					}
					if(cd){
						String disp = line.split(CONTENTDISP)[1];
						disp=disp.split(";")[0];
						contentDispositions.add(disp);
					}
				}
			}
			i++;
		}
	}


	public Set<String> getContentDispositions(){
		return contentDispositions;
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

	public class MailImage{
		private int startLine, endLine;

		public int getStartLine(){
			return startLine;
		}

		public int getEndLine(){
			return endLine;
		}
	}

	public String getDate() {
		return date;
	}

	public int getFromLine() {
		return fromLine;
	}

	public Set<String> getContentTypes() {
		return contentTypes;
	}

	public int getBodyIndex() {
		return bodyIndex;
	}

	public int getBodyEnd() {
		return bodyEnd;
	}

	public int getHtmlBeg() {
		return htmlBeg;
	}

	public int getHtmlEnd() {
		return htmlEnd;
	}

	public List<MailImage> getImages() {
		return photos;
	}

	public RandomAccessFile getReader() {
		return reader;
	}

	public RandomAccessFile getWriter() {
		return writer;
	}

	public String getFrom(){
		return from;
	}

	public boolean containsHeader(String header) throws IOException {
		RandomAccessFile r = new RandomAccessFile("./mail0.txt", "r");
		String line;
		while((line=r.readLine())!=null){
			if(line.equals("") || line.equals("\r")|| line.equals("\r\n")){
				return false;
			}
			if(line.startsWith(header)){
				return true;
			}
		}
		return false;
	}
}
