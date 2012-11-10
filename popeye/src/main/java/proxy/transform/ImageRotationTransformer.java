package proxy.transform;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

import proxy.Mail;
import proxy.Mail.MailImage;

public class ImageRotationTransformer implements MailTransformer{

	private static ImageRotationTransformer t;

	private ImageRotationTransformer(){

	}

	/** TEST METHOD
	public static void main(String args[]){
		ImageRotationTransformer imt = new ImageRotationTransformer();
		System.out.println(imt.transform(args[0].toString()));
	}*/

	/** Takes a base64 string, decodes it and creates a java2d image. 
	 *  Rotates the image, saves it to byte buffer and encodes it to base64 for return.
	 * @param the image to convert in String base64 format
	 * @return the image rotated, in String base64 format 
	 * @throws IOException 
	 */

	public void transform(Mail mail) throws IOException {
		List<String> list = new ArrayList<String>();
		int cant = mail.getImages().size();
		System.out.println("images to rotate: "+cant);
		if(cant==0){
			return;
		}

		File file = new File("./mails/mail"+mail.id()+"T.txt");
		file.createNewFile();
		File file2 = new File("./mails/mail"+mail.id()+".txt");
		RandomAccessFile reader = new RandomAccessFile("./mails/mail"+mail.id()+".txt", "r");
		RandomAccessFile writer = new RandomAccessFile("./mails/mail"+mail.id()+"T.txt", "rw");
		String line="";
		int i = 0;
		Iterator<MailImage> iter = mail.getImages().iterator();
		while(iter.hasNext()){
			MailImage image = iter.next();
			int beg = image.getStartLine();
			int end = image.getEndLine();
			while( i<beg && (line=reader.readLine())!=null){
				writer.write((line+"\r\n").getBytes());
				i++;
			}
			String base64 = line;
			while( i<=end && (line=reader.readLine())!=null){
				base64+=line;
				i++;
			}
			String rotated = imageRotation(base64);
			writer.write((rotated+"\r\n").getBytes());
		}
		while((line=reader.readLine())!=null){
			writer.write((line+"\r\n").getBytes());
		}
		file2.delete();
		file.renameTo(file2);
	}
	//TODO habira q actualizar indices
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
			e.printStackTrace();
			return image;
		}
		try {
			bos.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return image;
		}
		try {
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return image;
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

		tx.translate(image.getWidth()/2, image.getHeight()/2);

		tx.rotate(Math.PI); // 1 radians (180 degrees)

		// first - center image at the origin so rotate works OK
		tx.translate(-image.getWidth()/2,-image.getHeight()/2);

		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		System.out.println("Height: "+image.getHeight()+" width: "+image.getWidth());
		BufferedImage outputImage =new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		return op.filter(image, outputImage);
	}


	public static ImageRotationTransformer getInstance() {
		if(t==null){
			t=new ImageRotationTransformer();
		}
		return t;
	}


	@Override
	public String toString() {
		return "Image Rotation Transformer";
	}
}
