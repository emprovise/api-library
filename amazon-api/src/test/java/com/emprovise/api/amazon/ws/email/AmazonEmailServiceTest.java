package com.emprovise.api.amazon.ws.email;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AmazonEmailServiceTest {

    @Test
    public void sendEmail() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        AmazonEmailService awss3Service = new AmazonEmailService(credentials, Regions.US_EAST_1);
        List<String> rs = new ArrayList<>();
        rs.add("recepient@mail.com");
        String messageId = awss3Service.sendEmail("sender@mail.com", rs, "Hello !", "This is the first AWS message");
        System.out.println(messageId);
    }
}