package mta.smtp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

public class AsyncSMTPServer {

    private Selector selector;
    private ServerSocketChannel server;
    private volatile boolean running = false;

    public void start() throws IOException {

        selector = Selector.open();
        server = ServerSocketChannel.open();

        server.bind(new InetSocketAddress(0));
        server.configureBlocking(false);

        server.register(selector, SelectionKey.OP_ACCEPT);

        running = true;

        System.out.println("AsyncSMTPServer started on port 2525");


        while (running) {

            selector.select();

            Iterator<SelectionKey> keys =
                    selector.selectedKeys().iterator();

            while (keys.hasNext()) {

                SelectionKey key = keys.next();
                keys.remove();

                try {

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {

                        SocketChannel client = server.accept();
                        if (client != null) {
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                        }
                    }

                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();

                        try {
                            if (!client.isOpen()) {
                                key.cancel();
                                continue;
                            }

                            SMTPHandler.handle(client);

                        } catch (Exception e) {
                            System.err.println("Error handling client: " + e.getMessage());
                            e.printStackTrace();

                            key.cancel();
                            try {
                                if (client != null && client.isOpen()) client.close();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }

                } catch (IOException e) {
                    key.cancel();
                    if (key.channel() != null) key.channel().close();
                    e.printStackTrace();
                }
            }
        }

        close();
    }

    /**
     * Останавливает сервер
     */
    public void stop() {
        System.out.println("Stopping AsyncSMTPServer...");
        running = false;

        if (selector != null) {
            selector.wakeup();
        }
    }

    /**
     * Закрываем ресурсы
     */
    private void close() {
        try {
            if (server != null) server.close();
            if (selector != null) selector.close();
            System.out.println("AsyncSMTPServer stopped");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
