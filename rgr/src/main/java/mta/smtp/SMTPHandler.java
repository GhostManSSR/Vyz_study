package mta.smtp;

import mta.model.MailMessage;
import mta.queue.MailQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SMTPHandler {

    private static final Map<SocketChannel, ClientState> clientStates = new HashMap<>();
    private static final int BUFFER_SIZE = 1024;

    public static void handle(SocketChannel client) throws IOException {

        ClientState state = clientStates.computeIfAbsent(client, k -> new ClientState());

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int read = client.read(buffer);

        if (read == -1) {
            client.close();
            clientStates.remove(client);
            return;
        }

        buffer.flip();
        state.builder.append(StandardCharsets.US_ASCII.decode(buffer).toString());

        String[] lines = state.builder.toString().split("\r\n");
        for (int i = 0; i < lines.length - 1; i++) {
            processLine(client, lines[i], state);
        }

        state.builder = new StringBuilder(lines[lines.length - 1]);
    }

    private static void processLine(SocketChannel client, String line, ClientState state) throws IOException {
        line = line.trim();
        System.out.println("SMTP PACKET: " + line);

        if (state.inData) {
            if (line.equals(".")) {
                MailMessage msg = new MailMessage(state.sender, state.recipient, state.data.toString());
                MailQueue.enqueue(msg);
                client.write(ByteBuffer.wrap("250 Queued\r\n".getBytes(StandardCharsets.US_ASCII)));

                state.inData = false;
                state.data = new StringBuilder();
                state.sender = null;
                state.recipient = null;
            } else {
                state.data.append(line).append("\n");
            }
            return;
        }

        switch (line.toUpperCase()) {
            case "QUIT":
                client.write(ByteBuffer.wrap("221 Bye\r\n".getBytes(StandardCharsets.US_ASCII)));
                client.close();
                clientStates.remove(client);
                break;
            case "HELO":
            case "EHLO":
                client.write(ByteBuffer.wrap("250 Hello\r\n".getBytes(StandardCharsets.US_ASCII)));
                break;
            case "STARTTLS":
                client.write(ByteBuffer.wrap("454 TLS not available\r\n".getBytes(StandardCharsets.US_ASCII)));
                break;
            default:
                if (line.startsWith("MAIL FROM")) {
                    state.sender = SMTPParser.parseAddress(line);
                    client.write(ByteBuffer.wrap("250 OK\r\n".getBytes(StandardCharsets.US_ASCII)));
                } else if (line.startsWith("RCPT TO")) {
                    state.recipient = SMTPParser.parseAddress(line);
                    client.write(ByteBuffer.wrap("250 OK\r\n".getBytes(StandardCharsets.US_ASCII)));
                } else if (line.equals("DATA")) {
                    if (state.sender == null || state.recipient == null) {
                        client.write(ByteBuffer.wrap("503 Bad sequence of commands\r\n".getBytes(StandardCharsets.US_ASCII)));
                    } else {
                        state.inData = true;
                        client.write(ByteBuffer.wrap("354 End data with .\r\n".getBytes(StandardCharsets.US_ASCII)));
                    }
                } else {
                    client.write(ByteBuffer.wrap("500 Unknown command\r\n".getBytes(StandardCharsets.US_ASCII)));
                }
        }
    }

    private static class ClientState {
        StringBuilder builder = new StringBuilder();
        String sender = null;
        String recipient = null;
        StringBuilder data = new StringBuilder();
        boolean inData = false;
    }
}
