package nio.server;

import java.nio.channels.SelectionKey;

public interface OliveProtocol {
      void handleAccept(SelectionKey key);
      void handleRead(SelectionKey key);
      void handleWrite(SelectionKey key);
}