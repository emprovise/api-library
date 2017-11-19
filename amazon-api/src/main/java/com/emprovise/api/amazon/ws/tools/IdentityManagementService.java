package com.emprovise.api.amazon.ws.tools;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @see <a href="http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-iam-access-keys.html">IAM Access Keys</a>
 */
public class IdentityManagementService {

    private AmazonIdentityManagement identityManagement;

    public IdentityManagementService(AWSCredentials credentials, Regions region) {
        this(new AWSStaticCredentialsProvider(credentials), region);
    }

    public IdentityManagementService(AWSCredentials credentials, Regions region, ClientConfiguration config) {
        this(new AWSStaticCredentialsProvider(credentials), region, config);
    }

    public IdentityManagementService(AWSCredentialsProvider awsCredentialsProvider, Regions region) {
        this(awsCredentialsProvider, region, new ClientConfiguration());
    }

    public IdentityManagementService(AWSCredentialsProvider awsCredentialsProvider, Regions region, ClientConfiguration config) {
        this.identityManagement = AmazonIdentityManagementClientBuilder.standard()
                .withRegion(region)
                .withCredentials(awsCredentialsProvider)
                .withClientConfiguration(config)
                .build();
    }

    public User createUser(String userName) {
        CreateUserRequest request = new CreateUserRequest().withUserName(userName);
        return identityManagement.createUser(request).getUser();
    }

    public UpdateUserResult updateUser(String currentUserName, String newUserName) {
        UpdateUserRequest request = new UpdateUserRequest()
                                        .withUserName(currentUserName)
                                        .withNewUserName(newUserName);
        return identityManagement.updateUser(request);
    }

    public DeleteUserResult deleteUser(String userName) {
        DeleteUserRequest request = new DeleteUserRequest()
                .withUserName(userName);
        return identityManagement.deleteUser(request);
    }

    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();
        ListUsersResult response;

        do {
            ListUsersRequest request = new ListUsersRequest();
            response = identityManagement.listUsers(request);
            users.addAll(response.getUsers());
            request.setMarker(response.getMarker());

        }while(response.getIsTruncated());

        return users;
    }

    public CreateAccountAliasResult createAccountAlias(String alias) {
        CreateAccountAliasRequest request = new CreateAccountAliasRequest().withAccountAlias(alias);
        return identityManagement.createAccountAlias(request);
    }

    public List<String> getAccountAlias() {
        return identityManagement.listAccountAliases().getAccountAliases();
    }

    public DeleteAccountAliasResult deleteAccountAlias(String alias) {
        DeleteAccountAliasRequest request = new DeleteAccountAliasRequest()
                                                    .withAccountAlias(alias);
        return identityManagement.deleteAccountAlias(request);
    }

    public AccessKey createAccessKey(String userName) {
        CreateAccessKeyRequest request = new CreateAccessKeyRequest()
                                            .withUserName(userName);
        CreateAccessKeyResult response = identityManagement.createAccessKey(request);
        return response.getAccessKey();
    }

    public List<AccessKeyMetadata> getAllAccessKeys() {
        return getAllAccessKeys(null);
    }

    public List<AccessKeyMetadata> getAllAccessKeys(String username) {

        List<AccessKeyMetadata> accessKeyMetadataList = new ArrayList<>();
        ListAccessKeysResult response;

        do {
            ListAccessKeysRequest request = new ListAccessKeysRequest();

            if(username != null) {
                request = request.withUserName(username);
            }

            response = identityManagement.listAccessKeys(request);
            accessKeyMetadataList.addAll(response.getAccessKeyMetadata());
            request.setMarker(response.getMarker());

        }while(response.getIsTruncated());

        return accessKeyMetadataList;
    }

    public Date getAccessKeyLastUseDate(String accessKeyId) {
        GetAccessKeyLastUsedRequest request = new GetAccessKeyLastUsedRequest()
                                                    .withAccessKeyId(accessKeyId);
        GetAccessKeyLastUsedResult response = identityManagement.getAccessKeyLastUsed(request);
        return response.getAccessKeyLastUsed().getLastUsedDate();
    }

    public UpdateAccessKeyResult activateAccessKey(String accessKeyId, String userName) {
        return updateAccessKey(accessKeyId, userName, StatusType.Active);
    }

    public UpdateAccessKeyResult deactivateAccessKey(String accessKeyId, String userName) {
        return updateAccessKey(accessKeyId, userName, StatusType.Inactive);
    }

    public DeleteAccessKeyResult deleteAccessKey(String accessKeyId, String userName) {
        DeleteAccessKeyRequest request = new DeleteAccessKeyRequest()
                .withAccessKeyId(accessKeyId)
                .withUserName(userName);
        return identityManagement.deleteAccessKey(request);
    }

    public Policy createPolicy(String policyName, String policyDocument) {
        CreatePolicyRequest request = new CreatePolicyRequest()
                                            .withPolicyName(policyName)
                                            .withPolicyDocument(policyDocument);
        return identityManagement.createPolicy(request).getPolicy();
    }

    public Policy getPolicy(String policyArn) {
        GetPolicyRequest request = new GetPolicyRequest().withPolicyArn(policyArn);
        return identityManagement.getPolicy(request).getPolicy();
    }

    public AttachRolePolicyResult attachRolePolicy(String roleName, String policyArn) {

        ListAttachedRolePoliciesRequest request = new ListAttachedRolePoliciesRequest()
                                                        .withRoleName(roleName);
        List<AttachedPolicy> matchingPolicies = new ArrayList<>();
        ListAttachedRolePoliciesResult response;

        do {
            response = identityManagement.listAttachedRolePolicies(request);
            matchingPolicies.addAll(response.getAttachedPolicies()
                                    .stream()
                                    .filter(p -> p.getPolicyName().equals(roleName))
                                    .collect(Collectors.toList()));
        } while(response.getIsTruncated());

        if (!matchingPolicies.isEmpty()) {
            return new AttachRolePolicyResult();
        }

        AttachRolePolicyRequest attach_request = new AttachRolePolicyRequest()
                                                    .withRoleName(roleName)
                                                    .withPolicyArn(policyArn);
        return identityManagement.attachRolePolicy(attach_request);
    }

    public DetachRolePolicyResult detachRolePolicy(String roleName, String policyArn) {

        DetachRolePolicyRequest request = new DetachRolePolicyRequest()
                                                .withRoleName(roleName)
                                                .withPolicyArn(policyArn);
        return identityManagement.detachRolePolicy(request);
    }

    public List<ServerCertificateMetadata> getServerCertificates() {

        List<ServerCertificateMetadata> certificateMetadataList = new ArrayList<>();
        ListServerCertificatesResult response;

        do {
            ListServerCertificatesRequest request = new ListServerCertificatesRequest();
            response = identityManagement.listServerCertificates(request);
            certificateMetadataList.addAll(response.getServerCertificateMetadataList());
            request.setMarker(response.getMarker());
        }while(response.getIsTruncated());

        return certificateMetadataList;
    }

    public GetServerCertificateResult getServerCertificate(String certName) {
        GetServerCertificateRequest request = new GetServerCertificateRequest()
                                                    .withServerCertificateName(certName);
        return identityManagement.getServerCertificate(request);
    }

    public DeleteServerCertificateResult deleteServerCertificate(String certName) {
        DeleteServerCertificateRequest request = new DeleteServerCertificateRequest()
                                                        .withServerCertificateName(certName);
        return identityManagement.deleteServerCertificate(request);
    }

    public UpdateServerCertificateResult updateServerCertificate(String certName, String newCertName, String newPath) {
        UpdateServerCertificateRequest request = new UpdateServerCertificateRequest()
                                                        .withServerCertificateName(certName)
                                                        .withNewServerCertificateName(newCertName)
                                                        .withNewPath(newPath);
        return identityManagement.updateServerCertificate(request);
    }

    private UpdateAccessKeyResult updateAccessKey(String accessKeyId, String userName, StatusType status) {
        UpdateAccessKeyRequest request = new UpdateAccessKeyRequest()
                                            .withAccessKeyId(accessKeyId)
                                            .withUserName(userName)
                                            .withStatus(status);
        return identityManagement.updateAccessKey(request);
    }
}
