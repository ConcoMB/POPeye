package proxy.transform;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

import proxy.Mail;
import proxy.Mail.MailImage;

public class ImageRotationTransformer implements MailTransformer{

	/** TEST METHOD
	public static void main(String args[]){
		ImageRotationTransformer imt = new ImageRotationTransformer();
		System.out.println(imt.transform(args[0].toString()));
	}*/
	
	/** Takes a base64 string, decodes it and creates a java2d image. 
	 *  Rotates the image, saves it to byte buffer and encodes it to base64 for return.
	 * @param the image to convert in String base64 format
	 * @return the image rotated, in String base64 format 
	 */

	public void transform(Mail mail) {
		String message = mail.getMessage();
		List<String> list = new ArrayList<String>();
		for(MailImage mi: mail.getImages()){
			String rotated = imageRotation(mail.getImage(mi));
			list.add(rotated);
		}
		mail.replaceImages(list);
	}
	
	public String imageRotation(String image) {
		
		byte[] b  = decodeBase64(image);
		
		ByteArrayInputStream in = new ByteArrayInputStream(b);
	
		BufferedImage img = null;
		try {
			img = ImageIO.read(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		BufferedImage outputImg =rotateImage(img,180);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write( outputImg, "jpg", bos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			bos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] imageInByte = bos.toByteArray();
		try {
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] o = bos.toByteArray();
		
		return encodeBase64(o);
	
	}

	public byte[] decodeBase64(String s) {
	    return Base64.decodeBase64(s);
	}
	public String encodeBase64(byte[] b) {
	    return Base64.encodeBase64String(b);
	}

	public static BufferedImage rotateImage(BufferedImage image, double angle) {
		AffineTransform tx = new AffineTransform();
		tx.translate(image.getHeight()/2, image.getWidth()/2);
		tx.rotate(Math.PI); // 1 radians (180 degrees)
		
		// first - center image at the origin so rotate works OK
		tx.translate(-image.getWidth()/2,-image.getHeight()/2);
		
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		BufferedImage outputImage =new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
		return op.filter(image, outputImage);
	}

}
