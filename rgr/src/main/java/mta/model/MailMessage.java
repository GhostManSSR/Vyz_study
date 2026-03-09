package mta.model;

public class MailMessage {

    private int id;
    private String sender;
    private String recipient;
    private String body;
    private int retryCount;
    private boolean spam;
    private boolean isSpfPass;

    public MailMessage(String sender, String recipient, String body) {
        this.sender = sender;
        this.recipient = recipient;
        this.body = body;
        this.retryCount = 0;
        this.spam = false;
    }

    public void setSpfPass(boolean spf){
        isSpfPass = spf;
    }

    public boolean isSpfPass() {
        return isSpfPass;
    }

    public boolean isSpam() {
        return spam;
    }

    public void setSpam(boolean spam) {
        this.spam = spam;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incRetry() {
        retryCount++;
    }
}
