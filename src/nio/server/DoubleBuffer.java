package nio.server;

import java.nio.ByteBuffer;

public class DoubleBuffer {
	private StringBuffer readBuffer, writeBuffer;
	
	public DoubleBuffer(int n){
		readBuffer=new StringBuffer(n);
		writeBuffer=new StringBuffer(n);
	}

	public StringBuffer getReadBuffer() {
		return readBuffer;
	}

	public StringBuffer getWriteBuffer() {
		return writeBuffer;
	}
	
	
}
