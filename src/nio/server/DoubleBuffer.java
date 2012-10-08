package nio.server;

import java.nio.ByteBuffer;

public class DoubleBuffer {
	private ByteBuffer readBuffer, writeBuffer;
	
	public DoubleBuffer(int n){
		readBuffer=ByteBuffer.allocate(n);
		writeBuffer=ByteBuffer.allocate(n);
	}

	public ByteBuffer getReadBuffer() {
		return readBuffer;
	}

	public ByteBuffer getWriteBuffer() {
		return writeBuffer;
	}
	
	
}
