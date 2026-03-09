package mta.smtp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMTPParser {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("<([^>]+)>");

    public static String parseAddress(String line) {

        Matcher matcher = EMAIL_PATTERN.matcher(line);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    public static SMTPCommand parseCommand(String line) {

        line = line.toUpperCase();

        if (line.startsWith("HELO"))
            return SMTPCommand.HELO;

        if (line.startsWith("EHLO"))
            return SMTPCommand.EHLO;

        if (line.startsWith("MAIL FROM"))
            return SMTPCommand.MAIL_FROM;

        if (line.startsWith("RCPT TO"))
            return SMTPCommand.RCPT_TO;

        if (line.startsWith("DATA"))
            return SMTPCommand.DATA;

        if (line.startsWith("QUIT"))
            return SMTPCommand.QUIT;

        if (line.startsWith("STARTTLS"))
            return SMTPCommand.STARTTLS;

        return SMTPCommand.UNKNOWN;
    }

    public static String extractDomain(String email) {

        if (email == null)
            return null;

        int index = email.indexOf("@");

        if (index == -1)
            return null;

        return email.substring(index + 1);
    }

    public static boolean isValidEmail(String email) {

        if (email == null)
            return false;

        return email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        );
    }
}
