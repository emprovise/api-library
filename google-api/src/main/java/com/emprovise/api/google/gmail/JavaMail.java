package com.emprovise.api.google.gmail;

import com.emprovise.api.google.gmail.dao.EmailMessage;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Gmail by default prevents access to the apps and devices which use less secure sign-in technology to avoid gmail account to be more vulnerable.
 * This access can be turned off using the below security toggle.
 * @see <a href="https://myaccount.google.com/lesssecureapps?pli=1>Enable Allow less secure apps</a>
 */
public class JavaMail {

    private static final String GMAIL_POP3_HOST = "pop.gmail.com";
    private static final String GMAIL_POP3_PORT = "995";
    private static final String MAIL_STORE_TYPE = "pop3";

    public List<EmailMessage> fetchEmails(String username, String password) throws Exception {

            Properties properties = new Properties();
            properties.put("mail.pop3.host", GMAIL_POP3_HOST);
            properties.put("mail.pop3.port", GMAIL_POP3_PORT);
            properties.put("mail.pop3.starttls.enable", "true");
            properties.put("mail.pop3.socketFactory.class" , "javax.net.ssl.SSLSocketFactory" );

            Session emailSession = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore(MAIL_STORE_TYPE);
            store.connect(GMAIL_POP3_HOST, username, password);

            // create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            // retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            List<EmailMessage> emailMessages = new ArrayList<>();

            for (Message message : messages) {
                emailMessages.add(map(message));
            }

            // close the store and folder objects
            emailFolder.close(false);
            store.close();

            return emailMessages;
    }

    private EmailMessage map(Message message) throws IOException, MessagingException {
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setMessageId(message.getMessageNumber());
        emailMessage.setSender(Arrays.asList(message.getFrom()));
        emailMessage.setRecipients(Arrays.asList(message.getAllRecipients()));
        emailMessage.setSentDate(message.getSentDate());
        emailMessage.setReceivedDate(message.getReceivedDate());
        emailMessage.setSubject(message.getSubject());
        String content = getTextFromMessage(message);
        emailMessage.setContent(content);
        emailMessage.setFlags(message.getFlags());
        return emailMessage;
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break;
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception {

        JavaMail javaMail = new JavaMail();
        List<EmailMessage> emailMessages = javaMail.fetchEmails("emailaddress@gmail.com", "password");

        for (EmailMessage emailMessage : emailMessages) {
            System.out.println("---------------------------------");
            System.out.println("Email Number " + emailMessage.getMessageId());
            System.out.println("Subject: " + emailMessage.getSubject());
            System.out.println("From: " + emailMessage.getSender());
            System.out.println("Body: " + emailMessage.getContent());
        }
    }
}