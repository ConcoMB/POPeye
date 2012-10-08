package proxy.transform;


public class VowelMailTransformer implements MailTransformer {

	@Override
	public String transform(String message) {
		char[] string = message.toCharArray();
		for(int i=0; i<string.length; i++){
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
			}
		}
		return new String(string);
	}

}
