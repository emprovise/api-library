package com.emprovise.api.google.gmail.dao;

import javax.mail.Address;
import javax.mail.Flags;
import java.util.Date;
import java.util.List;

public class EmailMessage {

    private int messageId;
    private String subject;
    private Date sentDate;
    private Date receivedDate;
    private List<Address> sender;
    private List<Address> recipients;
    private Flags flags;
    private String content;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public List<Address> getSender() {
        return sender;
    }

    public void setSender(List<Address> sender) {
        this.sender = sender;
    }

    public List<Address> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Address> recipients) {
        this.recipients = recipients;
    }

    public Flags getFlags() {
        return flags;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
