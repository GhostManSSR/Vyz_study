package mta.smtp;

public enum SMTPCommand {

    HELO,
    EHLO,
    MAIL_FROM,
    RCPT_TO,
    DATA,
    QUIT,
    STARTTLS,
    UNKNOWN
}
