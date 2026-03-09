package mta.queue;

import mta.db.Database;
import mta.model.MailMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MailQueue {

    private static final java.util.Queue<MailMessage> queue = new java.util.LinkedList<>();

    public static synchronized void enqueue(MailMessage msg) {
        queue.add(msg);

        try (Connection conn = Database.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO messages (sender, recipient, body, spam, spf_pass) VALUES (?, ?, ?, ?, ?) RETURNING id"
            );

            ps.setString(1, msg.getSender());
            ps.setString(2, msg.getRecipient());
            ps.setString(3, msg.getBody());
            ps.setBoolean(4, msg.isSpam());
            ps.setBoolean(5, msg.isSpfPass());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                msg.setId(rs.getInt(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized MailMessage take() {
        return queue.poll();
    }

}
