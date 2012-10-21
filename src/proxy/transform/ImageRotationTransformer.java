package proxy.transform;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

public class ImageRotationTransformer implements MailTransformer{

	
	public static void main(String args[]){
		ImageRotationTransformer imt = new ImageRotationTransformer();
		System.out.println(imt.transform(args[0].toString()));
	}
	@Override
	public String transform(String message) {
		return imageRotation(message);
	}
	
	public String imageRotation(String image){
		
		byte[] b  = decodeBase64(image);
		
		ByteArrayInputStream in = new ByteArrayInputStream(b);
		
		BufferedImage img = null;
		try {
			img = ImageIO.read(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		BufferedImage outputImg =rotateImage(img, 90);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try {
			ImageIO.write(outputImg, "", bos);
		} catch (IOException e) {
			System.err.println("Failed to write image to buffer");
		}
		
		b = bos.toByteArray();
		
		return encodeBase64(b);
	
	}

	public byte[] decodeBase64(String s) {
	    return Base64.decodeBase64(s);
	}
	public String encodeBase64(byte[] b) {
	    return Base64.encodeBase64String(b);
	}


	public static BufferedImage rotateImage(BufferedImage image, double angle) {
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage outputImage =new BufferedImage(width, height, image.getType());
		Graphics2D g = outputImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.rotate(Math.toRadians(angle), width/2, height/2);
		g.drawImage(image, null, 0, 0);
		return outputImage;
	}

}
