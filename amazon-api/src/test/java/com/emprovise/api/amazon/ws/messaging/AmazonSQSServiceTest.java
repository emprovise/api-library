package com.emprovise.api.amazon.ws.messaging;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.model.Message;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class AmazonSQSServiceTest {

    @Test
    public void messaging() throws Exception {
        AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
        AmazonSQSService amazonSQSService = new AmazonSQSService(credentials, Regions.US_EAST_1);
        String queueName = "SeqQueue";
        String seqQueue = amazonSQSService.createQueue(queueName);
        amazonSQSService.sendMessage(queueName, "Sample Message to the Queue");

        List<Message> messages = amazonSQSService.receiveMessage(queueName);
        for (Message message : messages) {
            System.out.println("  Message");
            System.out.println("    MessageId:     " + message.getMessageId());
            System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
            System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
            System.out.println("    Body:          " + message.getBody());
            for (Map.Entry<String, String> entry : message.getAttributes().entrySet()) {
                System.out.println("  Attribute");
                System.out.println("    Name:  " + entry.getKey());
                System.out.println("    Value: " + entry.getValue());
            }

            amazonSQSService.deleteMessage(queueName, message);
        }

        amazonSQSService.deleteQueue(queueName);
    }
}