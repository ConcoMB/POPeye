package proxy.transform;

import proxy.Mail;


public class VowelTransformer implements MailTransformer {

	private static VowelTransformer t;
	
	private VowelTransformer(){}
	
	public void transform(Mail mail) {
		char[] string = mail.getMessage().toCharArray();
		int end = mail.getBodyEnd();
		int beg = mail.getBodyIndex(), i;
		int aux=0;
		for(i=0; aux!=beg; i++){
			if(string[i]=='\n'){
				aux++;
			}
		}
		aux=0;
		for(i++; i<string.length && aux<end-beg ; i++){
			switch(string[i]){
			case 'a':
				string[i]='4';
				break;
			case 'e':
				string[i]='3';
				break;
			case 'i':
				string[i]='1';
				break;
			case 'o':
				string[i]='0';
				break;
			case '\n':
				aux++;
				break;
			}
		}
		
		end = mail.getHTMLEnd();
		beg = mail.getHTMLIndex();
		aux=0;
		for(i=0; aux!=beg; i++){
			if(string[i]=='\n'){
				aux++;
			}
		}
		aux=0;
		boolean inTag=false;
		for(i++; i<string.length && aux<end-beg ; i++){
			switch(string[i]){
			case '<':
				inTag=true;
				break;
			case '>':
				inTag=false;
			case 'a':
				if(!inTag)
				string[i]='4';
				break;
			case 'e':
				if(!inTag)
				string[i]='3';
				break;
			case 'i':
				if(!inTag)
				string[i]='1';
				break;
			case 'o':
				if(!inTag)
				string[i]='0';
				break;
			case '\n':
				aux++;
				break;
			}
		}
		mail.setMessage(new String(string));
	}

	public static VowelTransformer getInstance() {
		if(t==null){
			t=new VowelTransformer();
		}
		return t;
	}

}
