package com.emprovise.api.amazon.ws.tools;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.amazonaws.services.identitymanagement.model.ServerCertificateMetadata;
import com.amazonaws.services.identitymanagement.model.User;
import com.amazonaws.services.s3.model.Bucket;
import com.emprovise.api.amazon.ws.storage.AmazonS3Service;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class IdentityManagementServiceTest {

    @Test
    public void getAllUsers() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        IdentityManagementService identityManagementService = new IdentityManagementService(credentials, Regions.US_EAST_1);
        List<User> users = identityManagementService.getAllUsers();
        System.out.println(users);
    }

    @Test
    public void getAccountAlias() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        IdentityManagementService identityManagementService = new IdentityManagementService(credentials, Regions.US_EAST_1);
        List<String> accountAlias = identityManagementService.getAccountAlias();
        System.out.println(accountAlias);
    }

    @Test
    public void getAllAccessKeys() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        IdentityManagementService identityManagementService = new IdentityManagementService(credentials, Regions.US_EAST_1);
        List<AccessKeyMetadata> accessKeys = identityManagementService.getAllAccessKeys();
        System.out.println(accessKeys);
    }

    @Test
    public void getServerCertificates() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        IdentityManagementService identityManagementService = new IdentityManagementService(credentials, Regions.US_EAST_1);
        List<ServerCertificateMetadata> certificates = identityManagementService.getServerCertificates();
        System.out.println(certificates);
    }
}