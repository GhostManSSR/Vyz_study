package mta.queue;

import mta.db.Database;
import mta.delivery.SMTPClient;
import mta.model.MailMessage;
import mta.dns.MXResolver;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RetryWorker implements Runnable {

    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 10_000;
    private static final long EMPTY_QUEUE_DELAY_MS = 100;

    @Override
    public void run() {
        while (true) {
            try {
                MailMessage msg = MailQueue.take();

                if (msg == null) {
                    Thread.sleep(EMPTY_QUEUE_DELAY_MS);
                    continue;
                }

                String recipient = msg.getRecipient();
                if (recipient == null || !recipient.contains("@")) {
                    System.err.println("Invalid recipient: " + recipient);
                    continue;
                }
                String domain = recipient.substring(recipient.indexOf("@") + 1);

                String mxHost = MXResolver.resolveMX(domain);
                if (mxHost == null || mxHost.isEmpty()) {
                    System.err.println("Cannot resolve MX for domain: " + domain);
                    continue;
                }

                boolean success = SMTPClient.send(msg, mxHost);

                if (success) {
                    try (Connection conn = Database.getConnection()) {
                        PreparedStatement ps = conn.prepareStatement(
                                "UPDATE messages SET sent = TRUE WHERE id = ?"
                        );
                        ps.setInt(1, msg.getId());
                        ps.executeUpdate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (msg.getRetryCount() < 5) {
                    msg.incRetry();
                    Thread.sleep(10000);
                    MailQueue.enqueue(msg);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
