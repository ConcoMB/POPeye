package nio.server;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class BufferUtils {
	public static String bufferToString(ByteBuffer buf) throws CharacterCodingException{
		return new String(buf.array(), 0, buf.remaining());
	}
}
