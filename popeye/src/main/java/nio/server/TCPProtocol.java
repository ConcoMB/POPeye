package nio.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.ParseException;

public interface TCPProtocol {
      void handleAccept(SelectionKey key) throws IOException;
      void handleRead(SelectionKey key) throws IOException, InterruptedException, ParseException;
      void handleWrite(SelectionKey key) throws IOException;
}