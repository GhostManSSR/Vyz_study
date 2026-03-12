package mta.smtp;

import mta.model.MailMessage;
import mta.queue.MailQueue;
import mta.security.DKIMSigner;
import mta.security.KeyProvider;
import mta.security.SPFValidator;
import mta.spam.SpamDetector;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

public class SMTPConnection implements Runnable {

    private Socket socket;
    private boolean isTLS = false;

    private StringBuilder data = new StringBuilder();
    private StringBuilder attachments = new StringBuilder();

    public SMTPConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        System.out.println("Client connected: " + socket.getInetAddress());

        try {

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            send(out, "220 MiniMTA");

            String sender = null;
            String recipient = null;

            boolean spfPass = false;
            boolean inData = false;

            String line;

            while ((line = in.readLine()) != null) {

                line = line.trim();

                System.out.println("SMTP PACKET: " + line);

                if (inData) {

                    if (line.equals(".")) {

                        processData(sender, recipient, spfPass,
                                data.toString(),
                                attachments.toString());

                        send(out, "250 Queued");

                        sender = null;
                        recipient = null;
                        spfPass = false;

                        data.setLength(0);
                        attachments.setLength(0);

                        inData = false;

                        continue;
                    }

                    data.append(line).append("\r\n");
                    continue;
                }

                String upper = line.toUpperCase();

                // EHLO / HELO
                if (upper.startsWith("EHLO") || upper.startsWith("HELO")) {

                    send(out, "250-MiniMTA");
                    send(out, "250-STARTTLS");
                    send(out, "250 OK");

                }

                // MAIL FROM
                else if (upper.startsWith("MAIL FROM")) {

                    sender = SMTPParser.parseAddress(line);

                    if (sender == null) {

                        send(out, "501 Bad address");
                        continue;
                    }

                    String domain = SMTPParser.extractDomain(sender);

                    if (domain.endsWith(".local") || domain.equals("localhost")) {

                        spfPass = true;
                        send(out, "250 OK");

                    } else {

                        spfPass = SPFValidator.checkSPF(domain);

                        if (!spfPass) {

                            sender = null;
                            send(out, "550 SPF check failed");

                        } else {

                            send(out, "250 OK");
                        }
                    }
                }

                // RCPT TO
                else if (upper.startsWith("RCPT TO")) {

                    if (sender == null) {

                        send(out, "503 Need MAIL FROM first");
                        continue;
                    }

                    recipient = SMTPParser.parseAddress(line);

                    if (recipient == null) {

                        send(out, "501 Bad address");

                    } else {

                        send(out, "250 OK");
                    }
                }

                // FILE (attachment)
                else if (upper.startsWith("FILE")) {

                    if (sender == null || recipient == null) {

                        send(out, "503 Need MAIL FROM and RCPT TO first");
                        continue;
                    }

                    try {

                        String path = line.substring(4).trim();
                        File file = new File(path);

                        if (!file.exists()) {

                            send(out, "550 File not found");
                            continue;
                        }

                        byte[] bytes = Files.readAllBytes(file.toPath());
                        String base64 = Base64.getEncoder().encodeToString(bytes);

                        String boundary = "----MiniMTABoundary";

                        attachments.append("--").append(boundary).append("\r\n");

                        attachments.append("Content-Type: application/octet-stream; name=\"")
                                .append(file.getName()).append("\"\r\n");

                        attachments.append("Content-Transfer-Encoding: base64\r\n");

                        attachments.append("Content-Disposition: attachment; filename=\"")
                                .append(file.getName()).append("\"\r\n\r\n");

                        attachments.append(base64).append("\r\n");

                        send(out, "250 File accepted");

                    } catch (Exception e) {

                        e.printStackTrace();
                        send(out, "550 File read error");
                    }
                }

                // DATA
                else if (upper.equals("DATA")) {

                    if (sender == null || recipient == null) {

                        send(out, "503 Bad sequence of commands");

                    } else {

                        inData = true;
                        send(out, "354 End data with <CR><LF>.<CR><LF>");
                    }
                }

                // RSET
                else if (upper.equals("RSET")) {

                    sender = null;
                    recipient = null;
                    spfPass = false;

                    data.setLength(0);
                    attachments.setLength(0);

                    inData = false;

                    send(out, "250 Reset OK");
                }

                // STARTTLS
                else if (upper.equals("STARTTLS")) {

                    if (isTLS) {

                        send(out, "454 TLS already active");
                        continue;
                    }

                    send(out, "220 Ready to start TLS");

                    try {

                        SSLContext sslContext = TLSUpgrade.createSSLContext();
                        SSLSocketFactory factory = sslContext.getSocketFactory();

                        SSLSocket sslSocket = (SSLSocket) factory.createSocket(
                                socket,
                                socket.getInetAddress().getHostName(),
                                socket.getPort(),
                                true);

                        sslSocket.setUseClientMode(false);
                        sslSocket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
                        sslSocket.startHandshake();

                        socket = sslSocket;

                        in = new BufferedReader(
                                new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.UTF_8));

                        out = new PrintWriter(
                                new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.UTF_8), true);

                        isTLS = true;

                        System.out.println("TLS established: "
                                + sslSocket.getSession().getProtocol()
                                + " / "
                                + sslSocket.getSession().getCipherSuite());

                    } catch (Exception e) {

                        e.printStackTrace();
                        send(out, "454 TLS not available");
                    }
                }

                // QUIT
                else if (upper.equals("QUIT")) {

                    send(out, "221 Bye");
                    socket.close();
                    return;
                }

                // UNKNOWN
                else {

                    send(out, "500 Unknown command");
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void send(PrintWriter out, String msg) {

        out.println(msg);
        System.out.println("SMTP SEND: " + msg);
    }

    private void processData(String sender,
                             String recipient,
                             boolean spfPass,
                             String messageBody,
                             String attachments) {

        try {

            boolean isSpam = SpamDetector.isSpam(messageBody);

            String dkimHeader = "";

            try {

                DKIMSigner signer = new DKIMSigner(KeyProvider.getPrivateKey());
                String signature = signer.sign(messageBody);

                dkimHeader = "DKIM-Signature: " + signature + "\r\n";

            } catch (Exception e) {

                e.printStackTrace();
            }

            String boundary = "----MiniMTABoundary";

            String fullMessage =
                    "From: <" + sender + ">\r\n" +
                            "To: <" + recipient + ">\r\n" +
                            "Subject: MiniMTA test\r\n" +
                            "MIME-Version: 1.0\r\n" +
                            "Content-Type: multipart/mixed; boundary=" + boundary + "\r\n" +
                            dkimHeader +
                            "\r\n" +

                            "--" + boundary + "\r\n" +
                            "Content-Type: text/plain; charset=UTF-8\r\n\r\n" +
                            messageBody + "\r\n" +

                            attachments +

                            "--" + boundary + "--\r\n";

            MailMessage msg = new MailMessage(sender, recipient, fullMessage);

            msg.setSpfPass(spfPass);
            msg.setSpam(isSpam);

            MailQueue.enqueue(msg);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
