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

import proxy.transform.ImageRotationTransformer;
import proxy.transform.VowelTransformer;

public class Mail {

	public static void main(String[] args) throws IOException, InterruptedException {
		BufferedReader b = new BufferedReader(new FileReader("./mails/mail0.txt"));
		Mail m = new Mail();
		String line;

		while((line=b.readLine())!=null){
			m.add(line+"\r\n");
		}
		m.parse();
		System.out.println(m.fromLine+" "+ m.date+" "+m.bodyEnd + " "+m.bodyIndex+ " "+m.from);
//		for(MailImage image: m.photos){
//			System.out.println(image.startLine);
//		}
		System.out.println(m.header);
		//AnonymousTransformer.getInstance().transform(m);
		//VowelTransformer.getInstance().transform(m);
		ImageRotationTransformer.getInstance().transform(m);
		//		ExternalAppExecuter a = new ExternalAppExecuter("./apps/toUpper.o");
		//		a.execute(m);
	}


	private static final String FROM = "From:", DATE="Date: ", MULTIPART= "Content-Type: multipart", CONTENTTYPE="Content-Type: ",
			TEXT="Content-Type: text/plain", CTE= "Content-Transfer-Encoding: ", PIC="Content-Type: image", 
			CONTENTDISP="Content-Disposition: ", HTML="Content-Type: text/html", Q_PRINT="Content-Transfer-Encoding: quoted-printable";
	private static int serial;
	public static int cantMails = 1000;
	
	private String date ;
	private int id;
	//	private String subject; // "Subject: ";
	private String from;
	private int fromLine;
	private Set<String> contentTypes = new HashSet<String>(), contentDispositions = new HashSet<String>();
	private int bodyIndex, bodyEnd, htmlBeg, htmlEnd;
	private List<MailImage> photos = new ArrayList<MailImage>();
	private RandomAccessFile reader, writer;
	private int size;
	private boolean quotedPrint;
	private String header="";

	public Mail() throws IOException {
		id=(serial++)%cantMails;
		File f = new File("./mails/mail"+id+".txt");
		f.delete();
		f.createNewFile();
		reader = new RandomAccessFile("./mails/mail"+id+".txt", "r");
		writer = new RandomAccessFile("./mails/mail"+id+".txt", "rw");
	}

	public void add(String line) throws IOException{
		size+=line.length();
		writer.write((line).getBytes());
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
			header+=line+"\r\n";
			if(line.toLowerCase().startsWith(FROM.toLowerCase())){
				//while(!line.contains("<")){i++;}
				//from = line.split("<")[1].split(">")[0];
				fromLine=i;
				from=line.split(FROM)[1];
			}else if(line.toLowerCase().startsWith(DATE.toLowerCase())){
				String[] d2 = line.split(" ");
				date = d2[2]+"/"+parseMonth(d2[3])+"/"+d2[4];
				//			}else if(nextBound!=null && line.equals("--"+nextBound)){
				//				bounds.add(nextBound);
				//				flag=true;
			}else if(line.toLowerCase().startsWith(CONTENTTYPE.toLowerCase())){
				contentTypes.add(line.split(CONTENTTYPE)[1]);
				if(line.startsWith(MULTIPART)){
					String b;
					while(line!=null && !line.contains("boundary")){
						i++;
						header+=line+"\r\n";
						line=reader.readLine();
					}
					header+=line+"\r\n";
					b = line.split("boundary=")[1];
					if(b.contains("\"")){
						b=b.split("\"")[1];
					}
					bounds.add(b);
				}else if (line.toLowerCase().startsWith(TEXT.toLowerCase())){
					while(line!=null & !line.equals("")){
						if(line.toLowerCase().startsWith(CONTENTDISP.toLowerCase())){
							String disp = line.split(CONTENTDISP)[1];
							disp=disp.split(";")[0];
							contentDispositions.add(disp);
						}
						if(line.toLowerCase().contains(Q_PRINT.toLowerCase())){
							quotedPrint=true;
						}
						header+=line+"\r\n";
						i++;
						line=reader.readLine();
					}
					header+=line+"\r\n";
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
				}else if(line.toLowerCase().startsWith(PIC.toLowerCase())){
					i++;
					line=reader.readLine();
					while(line!=null && !line.equals("")){
						if(line.toLowerCase().startsWith(CONTENTDISP.toLowerCase())){
							String disp = line.split(CONTENTDISP)[1];
							disp=disp.split(";")[0];
							contentDispositions.add(disp);
						}
						header+=line+"\r\n";
						i++;
						line=reader.readLine();
					}
					header+=line+"\r\n";
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
				}else if(line.toLowerCase().startsWith(HTML.toLowerCase())){
					while(line!=null && !line.equals("")){
						if(line.toLowerCase().contains(Q_PRINT.toLowerCase())){
							quotedPrint=true;
						}
						if(line.toLowerCase().startsWith(CONTENTDISP.toLowerCase())){
							String disp = line.split(CONTENTDISP)[1];
							disp=disp.split(";")[0];
							contentDispositions.add(disp);
						}
						header+=line+"\r\n";
						i++;
						line=reader.readLine();
					}
					header+=line+"\r\n";
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
					while(line!=null && !line.equals("")){
						if(line.toLowerCase().startsWith(CONTENTDISP.toLowerCase())){
							String disp = line.split(CONTENTDISP)[1];
							disp=disp.split(";")[0];
							contentDispositions.add(disp);
						}
						header+=line+"\r\n";
						i++;
						line=reader.readLine();
					}
					while(line!=null && line.equals("")){
						line=reader.readLine();
					}
					boolean bound = false;
					while(line!=null && !line.equals("") && !bound){
						for(String b: bounds){
							if(line.startsWith("--"+b) || line.equals(b)){
								bound=true;
								break;
							}
						}
						i++;
						line=reader.readLine();
					}
				}
			}
			i++;
		}
		reader.close();
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

	public boolean containsHeader(String regex){
//		RandomAccessFile r = new RandomAccessFile("./mails/mail"+id+".txt", "r");
//		String line;
//		while((line=r.readLine())!=null){
//			if(line.equals("") || line.equals("\r")|| line.equals("\r\n")){
//				return false;
//			}
//			if(line.toLowerCase().startsWith(header)){
//				return true;
//			}
//		}
//		return false;
		return header.toLowerCase().contains(regex.toLowerCase());
	}

	public int id(){
		return id;
	}

	public boolean hasQP() {
		return quotedPrint;
	}
}
