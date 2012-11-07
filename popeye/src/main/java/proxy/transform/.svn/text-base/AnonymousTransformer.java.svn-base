package proxy.transform;

import proxy.Mail;

public class AnonymousTransformer implements MailTransformer {
	
	private static AnonymousTransformer t;
	
	private AnonymousTransformer(){}
	
	public void transform(Mail mail) {		
		int index = mail.getFromLine();
		String[] s = mail.getMessage().split("\n");
		s[index]="From: Popeye's spinach <guess@who.com>\r";
		String m = "";
		for(String l:s){
			m+=l+"\n";
		}
		mail.setMessage(m);
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
