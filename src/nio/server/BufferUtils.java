package nio.server;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class BufferUtils {
	public static String bufferToString(ByteBuffer buf) throws CharacterCodingException{
		/*Charset charset = Charset.defaultCharset();  
        CharsetDecoder decoder = charset.newDecoder();  
        CharBuffer charBuffer = decoder.decode(buf);
        return buf.asCharBuffer().toString();*/
		return new String(buf.array(), 0, buf.remaining()).trim();
	}
}
