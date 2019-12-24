package com.emprovise.api.amazon.ws.storage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.emprovise.api.dto.AWSFile;
import org.junit.Test;

import java.io.File;
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
    public void uploadFile() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("A903555", "dm78fsen");
        AmazonS3Service amazonS3Service = new AmazonS3Service(credentials, Regions.US_EAST_1);
        File file = new File("C:/Users/pp63680/Downloads/MachineCart_C26710.pdf");
        ObjectMetadata metadata = amazonS3Service.uploadFile("om-jdsc-documents-devl", "temp", file);
        System.out.println(metadata);
    }

    @Test
    public void fetchFile() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("A903555", "dm78fsen");
        AmazonS3Service amazonS3Service = new AmazonS3Service(credentials, Regions.US_EAST_1);
        AWSFile awsFile = amazonS3Service.getAWSFile("om-jdsc-documents-devl", "MachineCart_C26710.pdf");

        FileOutputStream fileOuputStream = new FileOutputStream("C:/Users/pp63680/Downloads/MachineCart_C26710.pdf");
        fileOuputStream.write(awsFile.getFileBytes());
        fileOuputStream.close();
    }
}