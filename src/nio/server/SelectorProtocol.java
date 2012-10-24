package nio.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface SelectorProtocol {
      void handleAccept(SelectionKey key) throws IOException;
      void handleRead(SelectionKey key) throws IOException, InterruptedException;
      void handleWrite(SelectionKey key) throws IOException;
}