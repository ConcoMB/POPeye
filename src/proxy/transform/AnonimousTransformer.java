package proxy.transform;

public class AnonimousTransformer implements MailTransformer {
	
	@Override
	public String transform(String message) {
		return message.replaceAll("^From:[ ]*[a-zA-z ]*[ ]*<[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}>$", "From: Anonymous <anon@anon.org>");
	}

}
