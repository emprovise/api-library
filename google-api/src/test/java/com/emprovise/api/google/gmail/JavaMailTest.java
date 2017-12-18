package com.emprovise.api.google.gmail;

import com.emprovise.api.google.gmail.dao.EmailMessage;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class JavaMailTest {

    @Test
    public void fetchEmails() throws Exception {
        
        JavaMail javaMail = new JavaMail();
        List<EmailMessage> emailMessages = javaMail.fetchEmails("emailaddress@gmail.com", "password");
        Assert.assertNotNull(emailMessages);
        Assert.assertFalse(emailMessages.isEmpty());

        for (EmailMessage emailMessage : emailMessages) {
            System.out.println("---------------------------------");
            System.out.println("Email Number " + emailMessage.getMessageId());
            System.out.println("Subject: " + emailMessage.getSubject());
            System.out.println("From: " + emailMessage.getSender());
            System.out.println("Body: " + emailMessage.getContent());
        }
    }
}