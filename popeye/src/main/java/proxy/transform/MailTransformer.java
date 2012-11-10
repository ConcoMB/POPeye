package proxy.transform;

import java.io.FileNotFoundException;
import java.io.IOException;

import proxy.Mail;

public interface MailTransformer {

	public void transform(Mail mail) throws FileNotFoundException, IOException;
	
}
