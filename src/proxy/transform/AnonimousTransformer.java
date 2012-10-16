package proxy.transform;

public class AnonimousTransformer implements MailTransformer {

	@Override
	public String transform(String message) {
		String[] split = message.split("\n");
		String ans="";
		boolean flag=false;
		for(int i=0; i<split.length; i++){
			if(split[i].startsWith("From:")){
				if(flag){
					ans+=split[i];
				}else{
					flag=true;
				}
			}else{
				ans+=split[i];
			}
		}
		return ans;
	}

}
