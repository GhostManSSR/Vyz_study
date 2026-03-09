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

public class SMTPConnection implements Runnable {

    private Socket socket;
    private boolean isTLS = false;

    public SMTPConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));

            out.write("220 MiniMTA\r\n");
            out.flush();

            String sender = null;
            String recipient = null;
            boolean spfPass = false;
            boolean inData = false;
            StringBuilder data = new StringBuilder();

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                System.out.println("SMTP PACKET: " + line);

                // ===== DATA режим =====
                if (inData) {
                    if (line.equals(".")) {
                        processData(sender, recipient, spfPass, data.toString());
                        out.write("250 Queued\r\n");
                        out.flush();

                        sender = null;
                        recipient = null;
                        spfPass = false;
                        inData = false;
                        data = new StringBuilder();
                        continue;
                    } else {
                        data.append(line).append("\r\n");
                        continue;
                    }
                }

                String upper = line.toUpperCase();

                // ===== EHLO / HELO =====
                if (upper.startsWith("EHLO") || upper.startsWith("HELO")) {
                    out.write("250-MiniMTA\r\n");
                    out.write("250-STARTTLS\r\n");
                    out.write("250 OK\r\n");
                }
                // ===== MAIL FROM =====
                else if (upper.startsWith("MAIL FROM")) {
                    sender = SMTPParser.parseAddress(line);
                    if (sender == null) {
                        out.write("501 Bad address\r\n");
                        out.flush();
                        continue;
                    }

                    String domain = SMTPParser.extractDomain(sender);
                    if (domain.endsWith(".local") || domain.equals("localhost")) {
                        spfPass = true;
                        out.write("250 OK\r\n");
                    } else {
                        spfPass = SPFValidator.checkSPF(domain);
                        if (!spfPass) {
                            sender = null;
                            out.write("550 SPF check failed\r\n");
                        } else {
                            out.write("250 OK\r\n");
                        }
                    }
                }
                // ===== RCPT TO =====
                else if (upper.startsWith("RCPT TO")) {
                    if (sender == null) {
                        out.write("503 Need MAIL FROM first\r\n");
                    } else {
                        recipient = SMTPParser.parseAddress(line);
                        if (recipient == null) {
                            out.write("501 Bad address\r\n");
                        } else {
                            out.write("250 OK\r\n");
                        }
                    }
                }
                // ===== DATA =====
                else if (upper.equals("DATA")) {
                    if (sender == null || recipient == null) {
                        out.write("503 Bad sequence of commands\r\n");
                    } else {
                        inData = true;
                        out.write("354 End data with <CR><LF>.<CR><LF>\r\n");
                    }
                }
                // ===== RSET =====
                else if (upper.equals("RSET")) {
                    sender = null;
                    recipient = null;
                    spfPass = false;
                    data = new StringBuilder();
                    inData = false;
                    out.write("250 Reset OK\r\n");
                }
                // ===== QUIT =====
                else if (upper.equals("QUIT")) {
                    out.write("221 Bye\r\n");
                    out.flush();
                    socket.close();
                    return;
                }
                // ===== STARTTLS =====
                else if (upper.startsWith("STARTTLS")) {
                    if (isTLS) {
                        out.write("454 TLS already active\r\n");
                        out.flush();
                        continue;
                    }

                    out.write("220 Ready to start TLS\r\n");
                    out.flush();

                    try {
                        SSLContext sslContext = TLSUpgrade.createSSLContext();
                        SSLSocketFactory factory = sslContext.getSocketFactory();

                        SSLSocket sslSocket = (SSLSocket) factory.createSocket(
                                socket,
                                socket.getInetAddress().getHostAddress(),
                                socket.getPort(),
                                true
                        );

                        sslSocket.setUseClientMode(false);
                        sslSocket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
                        sslSocket.setEnableSessionCreation(true);
                        sslSocket.startHandshake();

                        in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.US_ASCII));
                        out = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.US_ASCII));
                        this.socket = sslSocket;
                        isTLS = true;

                        System.out.println("TLS established: " +
                                sslSocket.getSession().getProtocol() + " / " +
                                sslSocket.getSession().getCipherSuite());

                    } catch (Exception e) {
                        e.printStackTrace();
                        out.write("454 TLS not available\r\n");
                        out.flush();
                    }
                }
                // ===== Неизвестная команда =====
                else {
                    out.write("500 Unknown command\r\n");
                }

                out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processData(String sender, String recipient, boolean spfPass, String messageBody) {
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

            String fullMessage =
                    "From: <" + sender + ">\r\n" +
                            "To: <" + recipient + ">\r\n" +
                            "Subject: MiniMTA test\r\n" +
                            dkimHeader +
                            "\r\n" +
                            messageBody + "\r\n";

            MailMessage msg = new MailMessage(sender, recipient, fullMessage);
            msg.setSpfPass(spfPass);
            msg.setSpam(isSpam);

            MailQueue.enqueue(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
