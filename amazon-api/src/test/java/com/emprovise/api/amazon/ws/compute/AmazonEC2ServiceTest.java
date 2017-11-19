package com.emprovise.api.amazon.ws.compute;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.KeyPair;
import com.emprovise.api.amazon.ws.common.types.InternetProtocol;
import com.emprovise.api.amazon.ws.common.types.ProtocolFamily;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AmazonEC2ServiceTest {

    @Test
    public void authorizeSecurityGroup() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        AmazonEC2Service amazonEC2Service = new AmazonEC2Service(credentials, Regions.US_EAST_1);
        amazonEC2Service.createSecurityGroup("JavaSecurityGroup", "Test security group");
        List<String> ipAddresses = Arrays.asList("111.111.111.111/32", "150.150.150.150/32");
        amazonEC2Service.authorizeSecurityGroup("JavaSecurityGroup", ProtocolFamily.IPV4, ipAddresses, InternetProtocol.TCP, 22, 22);
    }

    @Test
    public void createKeyPair() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        AmazonEC2Service amazonEC2Service = new AmazonEC2Service(credentials, Regions.US_EAST_1);
        KeyPair keyPair = amazonEC2Service.createKeyPair("sampleKeyPair");
        System.out.println("Private Key: " + keyPair.getKeyMaterial());
    }

    @Test
    public void runInstance() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        AmazonEC2Service amazonEC2Service = new AmazonEC2Service(credentials, Regions.US_EAST_1);
        String instanceId = amazonEC2Service.runInstance("ami-da05a4a0", InstanceType.M1Small, "sampleKeyPair", "JavaSecurityGroup");
        System.out.println("Instance Id: " + instanceId);
    }

    @Test
    public void stopInstance() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        AmazonEC2Service amazonEC2Service = new AmazonEC2Service(credentials, Regions.US_EAST_1);
        amazonEC2Service.stopInstances("i-0f9fe64ba510df78a");
    }
}