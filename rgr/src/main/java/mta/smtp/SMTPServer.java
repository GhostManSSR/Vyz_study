package mta.smtp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SMTPServer {

    private int port;
    private ServerSocket server;
    private volatile boolean running = false;

    public SMTPServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = new ServerSocket(port);
        port = server.getLocalPort();
        running = true;

        System.out.println("SMTP server started on port " + port);

        while (running) {
            try {
                Socket socket = server.accept();
                new Thread(new SMTPConnection(socket)).start();
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                } else {
                    break;
                }
            }
        }

        close();
    }

    public void stop() {
        running = false;
        System.out.println("Stopping SMTP server...");

        try {
            if (server != null && !server.isClosed()) {
                server.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            if (server != null && !server.isClosed()) {
                server.close();
            }
            System.out.println("SMTP server stopped");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
