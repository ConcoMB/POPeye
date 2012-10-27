package proxy.transform;

import proxy.Mail;

public class AnonimousTransformer implements MailTransformer {
	
	private static AnonimousTransformer t;
	
	private AnonimousTransformer(){}
	
	public void transform(Mail mail) {
		String[] message = mail.getMessage().split("\n");
		String m ="";
		int l = mail.getFromLine();
		for(int j=0; j<message.length-1; j++){
			if(j!=l){
				m+=message[j]+"\n";
			}
		}
		
		//s.replaceFirst("^From:[ ]*[a-zA-z ]+[ ]*<[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}>$", "From: Anonymous <anon@anon.org>");
		//System.out.println(s);
		mail.setMessage(m);
		mail.eraseFrom();
	}

	public static MailTransformer getInstance() {
		if(t==null){
			t=new AnonimousTransformer();
		}
		return t;
	}

}
