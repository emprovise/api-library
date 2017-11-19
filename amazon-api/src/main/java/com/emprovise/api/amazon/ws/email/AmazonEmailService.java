package com.emprovise.api.amazon.ws.email;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import java.util.List;

/**
 * @see <a href="http://docs.aws.amazon.com/ses/latest/DeveloperGuide/verify-email-addresses.html">Verify Email Addresses</a>
 * <a href="http://docs.aws.amazon.com/ses/latest/DeveloperGuide/sending-email.html">Sending Email</a>
 *
 */
public class AmazonEmailService {

    private AmazonSimpleEmailService amazonEmailService;

    public AmazonEmailService(AWSCredentials credentials, Regions region) {
        this(new AWSStaticCredentialsProvider(credentials), region);
    }

    public AmazonEmailService(AWSCredentials credentials, Regions region, ClientConfiguration config) {
        this(new AWSStaticCredentialsProvider(credentials), region, config);
    }

    public AmazonEmailService(AWSCredentialsProvider awsCredentialsProvider, Regions region) {
        this(awsCredentialsProvider, region, new ClientConfiguration());
    }

    public AmazonEmailService(AWSCredentialsProvider awsCredentialsProvider, Regions region, ClientConfiguration config) {
        this.amazonEmailService =  AmazonSimpleEmailServiceClientBuilder.standard()
                                        .withCredentials(awsCredentialsProvider)
                                        .withRegion(region)
                                        .withClientConfiguration(config)
                                        .build();
    }

    /**
     * While running in sandbox mode email can be sent only to verified users, once account is activated for production email can be sent to anyone.
     * @param sender
     * @param recipientsList
     * @param subjectText
     * @param messageText
     * @return
     * @throws Exception
     */
    public String sendEmail(String sender, List<String> recipientsList, String subjectText, String messageText) throws Exception {

        String[] recipients = recipientsList.toArray(new String[recipientsList.size()]);
        Destination destination = new Destination().withToAddresses(recipients);

        // Create the subject and body of the message.
        Content subject = new Content().withData(subjectText);
        Content textBody = new Content().withData(messageText);
        Body body = new Body().withText(textBody);

        // Create a message with the specified subject and body.
        Message message = new Message().withSubject(subject).withBody(body);
        SendEmailRequest request = new SendEmailRequest().withSource(sender).withDestination(destination).withMessage(message);
        SendEmailResult sendEmailResult = amazonEmailService.sendEmail(request);
        return sendEmailResult.getMessageId();
    }
}
