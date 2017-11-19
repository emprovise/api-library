package com.emprovise.api.amazon.ws.storage;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Amazon Simple Storage Service
 * @see <a href="http://docs.aws.amazon.com/AmazonS3/latest/dev/Introduction.html">Amazon S3 Introduction</a>
 * <a href="https://javatutorial.net/java-s3-example">Java S3 Example</a>
 */
public class AmazonS3Service {

    private AmazonS3 amazonS3;

    private static final String SUFFIX = "/";

    public AmazonS3Service(AWSCredentials credentials, Regions region) {
        this(new AWSStaticCredentialsProvider(credentials), region);
    }

    public AmazonS3Service(AWSCredentials credentials, Regions region, ClientConfiguration config) {
        this(new AWSStaticCredentialsProvider(credentials), region, config);
    }

    public AmazonS3Service(AWSCredentialsProvider awsCredentialsProvider, Regions region) {
        this(awsCredentialsProvider, region, new ClientConfiguration());
    }

    public AmazonS3Service(AWSCredentialsProvider awsCredentialsProvider, Regions region, ClientConfiguration config) {
        this.amazonS3 =  AmazonS3ClientBuilder.standard()
                            .withRegion(region)
                            .withCredentials(awsCredentialsProvider)
                            .withClientConfiguration(config)
                            .build();
    }

    /**
     * S3 uses a single namespace across accounts and across regions.
     * Choose a name that is both legal by the naming conventions, and also not in use by any account, not just your own accounts.
     * @param bucketName
     * @return
     */
    public Bucket createBucket(String bucketName) {
        return amazonS3.createBucket(bucketName);
    }

    public void deleteBucket(String bucketName) {
        amazonS3.deleteBucket(bucketName);
    }

    public List<Bucket> listAllBuckets() {
        return amazonS3.listBuckets();
    }

    public List<S3ObjectSummary> listAllObjects(String bucketName) {
        ObjectListing objects = amazonS3.listObjects(bucketName);
        return objects.getObjectSummaries();
    }

    public void createDirectory(String bucketName, String directoryName) {
        // create meta-data for your directory and set content-length to 0
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);

        // create empty content
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

        // create a PutObjectRequest passing the directory name suffixed by /
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, directoryName + SUFFIX, emptyContent, metadata);

        // send request to S3 to create directory
        amazonS3.putObject(putObjectRequest);
    }

    /**
     * Deletes all the files within the directory along with the directory itself.
     */
    public void deleteDirectory(String bucketName, String directoryName) {
        List<S3ObjectSummary> fileList = amazonS3.listObjects(bucketName, directoryName).getObjectSummaries();

        for (S3ObjectSummary file : fileList) {
            amazonS3.deleteObject(bucketName, file.getKey());
        }
        amazonS3.deleteObject(bucketName, directoryName);
    }

    public void uploadFile(String bucketName, String destinationPath, File targetFile) {
        amazonS3.putObject(new PutObjectRequest(bucketName, destinationPath, targetFile)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public byte[] getFileBytes(String bucketName, String fileKey) throws IOException {
        S3Object object = amazonS3.getObject(new GetObjectRequest(bucketName, fileKey));
        InputStream objectData = object.getObjectContent();
        byte[] bytes = IOUtils.toByteArray(objectData);
        objectData.close();
        return bytes;
    }
}
