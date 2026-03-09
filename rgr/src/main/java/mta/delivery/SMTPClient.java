package mta.delivery;

import mta.model.MailMessage;

import java.io.*;
import java.net.Socket;

public class SMTPClient {

    private static final boolean TEST_MODE = true;

    public static boolean send(MailMessage msg, String mxHost) {

        String host;
        int port;

        if (TEST_MODE) {
            host = "localhost";
            port = 1025;
        } else {
            host = mxHost;
            port = 25;
        }

        try (
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {

            System.out.println("Connecting to SMTP: " + host + ":" + port);

            System.out.println("SERVER: " + in.readLine());

            out.write("HELO minimta\r\n");
            out.flush();
            System.out.println("SERVER: " + in.readLine());

            out.write("MAIL FROM:<" + msg.getSender() + ">\r\n");
            out.flush();
            System.out.println("SERVER: " + in.readLine());

            out.write("RCPT TO:<" + msg.getRecipient() + ">\r\n");
            out.flush();
            System.out.println("SERVER: " + in.readLine());

            out.write("DATA\r\n");
            out.flush();
            System.out.println("SERVER: " + in.readLine());

            out.write(msg.getBody() + "\r\n.\r\n");
            out.flush();
            System.out.println("SERVER: " + in.readLine());

            out.write("QUIT\r\n");
            out.flush();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
