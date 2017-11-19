package com.emprovise.api.amazon.ws.storage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.Bucket;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.List;

public class AmazonS3ServiceTest {

    @Test
    public void listAllBuckets() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        AmazonS3Service amazonS3Service = new AmazonS3Service(credentials, Regions.US_EAST_1);
        List<Bucket> buckets = amazonS3Service.listAllBuckets();
        System.out.println(buckets);
    }

    @Test
    public void createDirectory() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        AmazonS3Service amazonS3Service = new AmazonS3Service(credentials, Regions.US_EAST_1);
        amazonS3Service.createDirectory("testbucket", "data");
    }

    @Test
    public void getFileBytes() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        AmazonS3Service amazonS3Service = new AmazonS3Service(credentials, Regions.US_EAST_1);
        byte[] fileBytes = amazonS3Service.getFileBytes("testbucket", "73b210aab346e785d095eb53c29f4fa8.jpg");

        FileOutputStream fileOuputStream = new FileOutputStream("C:/testfile.jpg");
        fileOuputStream.write(fileBytes);
        fileOuputStream.close();
    }
}