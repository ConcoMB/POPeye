package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.text.ParseException;
import java.util.Iterator;

public class PopeyeServer {
	private static final int BUFSIZE = 1024 * 1024; // Buffer size (bytes)
	private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)
	private static final int defaultPort = 110;

	public static void main(String[] args) throws IOException, InterruptedException, ParseException {
		int ports[]={4040};
		/*if (args.length < 1) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Port> ...");
        }*/
		// Create a selector to multiplex listening sockets and connections
		Selector selector = Selector.open();
		Selector oliveSelector = Selector.open();
		Selector blutoSelector = Selector.open();
		// Create listening socket channel for each port and register selector
		for (Integer port : ports) {
			ServerSocketChannel listnChannel = ServerSocketChannel.open();
			listnChannel.socket().bind(new InetSocketAddress(port));
			listnChannel.configureBlocking(false); // must be nonblocking to
			// register
			// Register selector with channel. The returned key is ignored
			listnChannel.register(selector, SelectionKey.OP_ACCEPT);
		}
		ServerSocketChannel listnChannel = ServerSocketChannel.open();
		listnChannel.socket().bind(new InetSocketAddress(4444));
		listnChannel.configureBlocking(false); // must be nonblocking to
		// register
		// Register selector with channel. The returned key is ignored
		listnChannel.register(oliveSelector, SelectionKey.OP_ACCEPT);
		listnChannel = ServerSocketChannel.open();
		listnChannel.socket().bind(new InetSocketAddress(8888));
		listnChannel.configureBlocking(false); // must be nonblocking to
		// register
		// Register selector with channel. The returned key is ignored
		listnChannel.register(blutoSelector, SelectionKey.OP_ACCEPT);
		
		// Create a handler that will implement the protocol
		SelectorProtocol protocol = new PopSelectorProtocol(BUFSIZE,defaultPort,selector);
		SelectorProtocol oliveProtocol = new OliviaSelectorProtocol(BUFSIZE,oliveSelector);
		SelectorProtocol blutoProtocol = new BrutusSelectorProtocol(BUFSIZE,blutoSelector);
		Thread oliveThread = new SelectorThread(oliveSelector, oliveProtocol);
		Thread blutoThread = new SelectorThread(blutoSelector, blutoProtocol);
		oliveThread.start();
		blutoThread.start();
		while (true) { // Run forever, processing available I/O operations
			// Wait for some channel to be ready (or timeout)
			if (selector.select(TIMEOUT) == 0) { // returns # of ready chans
				//System.out.println(".");
				continue;
			}
			handleKeys(selector,protocol);
		}
	}

	private static void handleKeys(Selector selector, SelectorProtocol protocol)
			throws IOException, InterruptedException, ParseException {
		// Get iterator on set of keys with I/O to process
		Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
		while (keyIter.hasNext()) {
			SelectionKey key = keyIter.next(); // Key is bit mask
			// Server socket channel has pending connection requests?
			if (key.isValid() && key.isAcceptable()) {
				// TODO
				protocol.handleAccept(key);
			}
			// Client socket channel has pending data?
			if (key.isValid() && key.isReadable()) {
				protocol.handleRead(key);
			}
			// Client socket channel is available for writing and
			// key is valid (i.e., channel not closed)?
			if (key.isValid() && key.isWritable()) {
				protocol.handleWrite(key);
			}
			keyIter.remove(); // remove from set of selected keys
		}
	}

	private static class SelectorThread extends Thread {
		private Selector selector;
		private SelectorProtocol protocol;

		public SelectorThread(Selector selector, SelectorProtocol protocol) {
			this.selector = selector;
			this.protocol = protocol;
		}

		@Override
		public void run() {
			try{
			while (true) { // Run forever, processing available I/O operations
				// Wait for some channel to be ready (or timeout)
				if (selector.select(TIMEOUT) == 0) { // returns # of ready chans
					//System.out.println(".");
					continue;
				}
				handleKeys(selector,protocol);
			}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}