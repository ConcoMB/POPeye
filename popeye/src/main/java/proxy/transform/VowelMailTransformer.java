package proxy.transform;

import proxy.Mail;


public class VowelMailTransformer implements MailTransformer {

	
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
		mail.setMessage(new String(string));
	}

}
