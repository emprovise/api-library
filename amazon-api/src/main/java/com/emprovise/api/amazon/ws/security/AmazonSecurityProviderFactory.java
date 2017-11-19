package com.emprovise.api.amazon.ws.security;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.*;

import java.util.UUID;

public class AmazonSecurityProviderFactory {

    /**
     * @see <a href="http://docs.aws.amazon.com/AmazonS3/latest/dev/AuthUsingTempFederationTokenJava.html">Using Federated User Temporary Credentials</a>
     * <a href="http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-access-control.html">Access Control Policies</a>
     * @param credentialsProvider
     * @param user {@link String} username
     * @param duration in seconds
     * @param policyStatements {@link Statement} containing AWS access control policies
     * @return
     */
    public AWSSessionCredentials getFederatedUserSessionCredentials(AWSCredentialsProvider credentialsProvider, int duration,
                                                                    String user, Statement... policyStatements) {

        AWSSecurityTokenService securityTokenService = AWSSecurityTokenServiceClientBuilder.standard()
                                                            .withCredentials(credentialsProvider)
                                                            .build();

        GetFederationTokenRequest getFederationTokenRequest = new GetFederationTokenRequest();
        getFederationTokenRequest.setDurationSeconds(duration);
        getFederationTokenRequest.setName(user);

        // Define the policy and add to the request.
        Policy policy = new Policy().withStatements(policyStatements);
        // Add the policy to the request.
        getFederationTokenRequest.setPolicy(policy.toJson());

        GetFederationTokenResult federationTokenResult = securityTokenService.getFederationToken(getFederationTokenRequest);
        Credentials credentials = federationTokenResult.getCredentials();
        return getBasicSessionCredentials(credentials);
    }

    /**
     * @see <a href="http://docs.aws.amazon.com/AmazonS3/latest/dev/AuthUsingTempSessionTokenJava.html">Using IAM User Temporary Credentials</a>
     * @param credentialsProvider
     * @param duration in seconds
     * @return
     */
    public AWSSessionCredentials getUserSessionCredentials(AWSCredentialsProvider credentialsProvider, int duration) {

        AWSSecurityTokenService securityTokenService = AWSSecurityTokenServiceClientBuilder.standard()
                                                        .withCredentials(credentialsProvider)
                                                        .build();

        GetSessionTokenRequest getSessionTokenRequest = new GetSessionTokenRequest();
        getSessionTokenRequest.setDurationSeconds(duration);
        GetSessionTokenResult sessionTokenResult = securityTokenService.getSessionToken(getSessionTokenRequest);
        Credentials credentials = sessionTokenResult.getCredentials();
        return getBasicSessionCredentials(credentials);
    }

    /**
     * @see <a href="http://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">AWS ARN and Namespaces</a>
     * @param credentialsProvider
     * @param duration
     * @param principalArn
     * @param roleArn
     * @param samlAssertion
     * @return
     */
    public AWSSessionCredentials getAssumedRoleWithSAMLCredentials(AWSCredentialsProvider credentialsProvider, int duration,
                                                                   String principalArn, String roleArn, String samlAssertion) {

        AWSSecurityTokenService securityTokenService = AWSSecurityTokenServiceClientBuilder.standard()
                                                        .withCredentials(credentialsProvider)
                                                        .build();

        AssumeRoleWithSAMLRequest assumeRequest = new AssumeRoleWithSAMLRequest()
                                                        .withPrincipalArn(principalArn)
                                                        .withRoleArn(roleArn)
                                                        .withSAMLAssertion(samlAssertion)
                                                        .withDurationSeconds(duration);

        AssumeRoleWithSAMLResult assumeRoleWithSAMLResult = securityTokenService.assumeRoleWithSAML(assumeRequest);
        Credentials credentials = assumeRoleWithSAMLResult.getCredentials();
        return getBasicSessionCredentials(credentials);
    }

    public AWSSessionCredentials getAssumedRoleCredentials(AWSCredentialsProvider credentialsProvider, int duration, String roleArn) {

        AWSSecurityTokenService securityTokenService = AWSSecurityTokenServiceClientBuilder.standard()
                                                        .withCredentials(credentialsProvider)
                                                        .build();

        AssumeRoleResult assumeRoleResult = securityTokenService.assumeRole(new AssumeRoleRequest()
                .withRoleArn(roleArn)
                .withDurationSeconds(duration)
                .withRoleSessionName(UUID.randomUUID().toString())
        );

        Credentials credentials = assumeRoleResult.getCredentials();
        return getBasicSessionCredentials(credentials);
    }

    private BasicSessionCredentials getBasicSessionCredentials(Credentials sessionCredentials) {
        return new BasicSessionCredentials(
                sessionCredentials.getAccessKeyId(),
                sessionCredentials.getSecretAccessKey(),
                sessionCredentials.getSessionToken());
    }
}
