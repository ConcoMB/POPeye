package configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import proxy.POPeye;
import user.User;

public class Configuration {

	private POPeye proxy;

	private enum Variable{
		HOUR_MINHOUR, HOUR_MINMINUTE, HOUR_MAXHOUR, 
		HOUR_MAXMINUTE, CANT, SERVER, ERASE_DATE, ERASE_FROM,
		ERSE_CONTENTTYPE, ERASE_MINSIZE, ERASE_MAXSIZE, ERASE_ATTACHMENT,
		ERASE_PICTURE;
	}

	public Configuration(POPeye proxy){
		this.proxy=proxy;
	}

	public void apply(String command) throws IOException{
//		String[] splitted = command.split(" ");
//		if(splitted.length<6 || !splitted[0].equals("IN") ||
//				!splitted[2].equals("SET") || !splitted[4].equals("VALUE")){
//			System.out.println("not valid config");
//			return;
//		}
//		
//		User user;
//		if(splitted[1].equals(proxy.getCurrentUserName())){
//			user = proxy.getCurrentUser();
//		}
//		Variable v;
//		int val;
//		try{
//			val=Integer.valueOf(splitted[5]);
//			v=Variable.valueOf(splitted[3]);
//			
//		}catch(Exception e){
//			System.out.println("not valid config");
//			return;
//		}
//		File readFile, writeFile, auxFile = new File("./auxConf.txt");
//		BufferedReader read;
//		BufferedWriter write = new BufferedWriter(new FileWriter("./auxConf.txt"));
//		String line;
//		String[] s, min, max;
//		if(splitted[3].startsWith("HOUR")){
//			readFile=new File("./hourDenial_"+splitted[1]+".txt");
//			writeFile = new File("./hourDenial_"+splitted[1]+".txt");
//			read = new BufferedReader(new FileReader("./hourDenial_"+splitted[1]+".txt"));
//			line= read.readLine();
//			s = line.split(",");
//			min = s[0].split(":");
//			max = s[1].split(":");			
//		}
//		read.close();
//		switch(v){
//		case HOUR_MINHOUR:
//			min[0]=splitted[5];
//			//escribir en auxConf.
//			readFile.delete();
//			auxFile.renameTo(writeFile);
//			break;
//		case HOUR_MINMINUTE: 
//			break;
//		case HOUR_MAXHOUR:
//			break;
//		case HOUR_MAXMINUTE: 
//			break;
//		case CANT: 
//			break;
//		case SERVER: 
//			break;
//		case ERASE_DATE: 
//			break;
//		case ERASE_FROM:
//			break;
//		case ERSE_CONTENTTYPE:
//			break;
//		case ERASE_MINSIZE: 
//			break;
//		case ERASE_MAXSIZE: 
//			break;
//		case ERASE_ATTACHMENT:
//			break;
//		case ERASE_PICTURE:
//			break;
//		}

	}
}
