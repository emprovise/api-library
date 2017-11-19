package com.emprovise.api.amazon.ws.security;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.auth.policy.resources.S3ObjectResource;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.emprovise.api.amazon.ws.storage.AmazonS3Service;
import org.junit.Test;

public class AmazonSecurityProviderFactoryTest {

    @Test
    public void getFederatedUserSessionCredentials() throws Exception {

        String bucketName = "MyA1Bucket";
        AmazonSecurityProviderFactory amazonSecurityProviderFactory = new AmazonSecurityProviderFactory();

        Statement allowPublicReadStatement = new Statement(Statement.Effect.Allow)
                .withPrincipals(Principal.AllUsers)
                .withActions(S3Actions.GetObject)
                .withResources(new S3ObjectResource(bucketName, "*"));
        Statement allowRestrictedWriteStatement = new Statement(Statement.Effect.Allow)
                .withPrincipals(new Principal("123456789"), new Principal("876543210"))
                .withActions(S3Actions.PutObject)
                .withResources(new S3ObjectResource(bucketName, "*"));

        AWSSessionCredentials credentials = amazonSecurityProviderFactory.getFederatedUserSessionCredentials(new ProfileCredentialsProvider(), 7200,
                "User1", allowPublicReadStatement, allowRestrictedWriteStatement);
        AmazonS3Service s3Service = new AmazonS3Service(credentials, Regions.US_EAST_1);
        s3Service.listAllObjects(bucketName);
    }

}