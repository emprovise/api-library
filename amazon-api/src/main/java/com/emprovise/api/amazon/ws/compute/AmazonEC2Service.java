package com.emprovise.api.amazon.ws.compute;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.emprovise.api.amazon.ws.common.types.InternetProtocol;
import com.emprovise.api.amazon.ws.common.types.ProtocolFamily;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @see <a href="https://github.com/awsdocs/aws-doc-sdk-examples">Amazon EC2 Examples</a>
 */
public class AmazonEC2Service {

    private AmazonEC2 amazonEC2;

    public AmazonEC2Service(AWSCredentials credentials, Regions region) {
        this(new AWSStaticCredentialsProvider(credentials), region);
    }

    public AmazonEC2Service(AWSCredentials credentials, Regions region, ClientConfiguration config) {
        this(new AWSStaticCredentialsProvider(credentials), region, config);
    }

    public AmazonEC2Service(AWSCredentialsProvider awsCredentialsProvider, Regions region) {
        this(awsCredentialsProvider, region, new ClientConfiguration());
    }

    public AmazonEC2Service(AWSCredentialsProvider awsCredentialsProvider, Regions region, ClientConfiguration config) {
        this.amazonEC2 = AmazonEC2ClientBuilder.standard()
                                                .withCredentials(awsCredentialsProvider)
                                                .withRegion(region)
                                                .withClientConfiguration(config)
                                                .build();
    }

    public String runInstance(String imageId, InstanceType instanceType, String keyName, String groupName) {
        RunInstancesRequest runRequest = new RunInstancesRequest()
                .withImageId(imageId)
                .withInstanceType(instanceType)
                .withMaxCount(1)
                .withMinCount(1)
                .withKeyName(keyName)
                .withSecurityGroups(groupName);

        RunInstancesResult runInstancesResult = amazonEC2.runInstances(runRequest);
        return runInstancesResult.getReservation().getReservationId();
    }

    public CreateTagsResult createTag(String key, String value) {
        Tag tag = new Tag().withKey(key).withValue(value);
        CreateTagsRequest tagRequest = new CreateTagsRequest().withTags(tag);
        return amazonEC2.createTags(tagRequest);
    }

    public CreateTagsResult createTagsForResources(Map<String, String> keyValueMap, String... resourceIds) {
        List<Tag> tags = keyValueMap.entrySet().stream()
                                    .map(entry -> new Tag(entry.getKey(), entry.getValue()))
                                    .collect(Collectors.toList());

        CreateTagsRequest tagRequest = new CreateTagsRequest(Arrays.asList(resourceIds), tags);
        return amazonEC2.createTags(tagRequest);
    }

    public String allocateAddress() {
        AllocateAddressRequest allocateRequest = new AllocateAddressRequest().withDomain(DomainType.Vpc);
        AllocateAddressResult allocateResponse = amazonEC2.allocateAddress(allocateRequest);
        return allocateResponse.getAllocationId();
    }

    public AssociateAddressResult associateAddress(String  instanceId, String allocationId) {
        AssociateAddressRequest associateRequest = new AssociateAddressRequest()
                                                            .withInstanceId(instanceId)
                                                            .withAllocationId(allocationId);
        return amazonEC2.associateAddress(associateRequest);
    }

    public KeyPair createKeyPair(String keyName) {
        CreateKeyPairRequest request = new CreateKeyPairRequest().withKeyName(keyName);
        CreateKeyPairResult response = amazonEC2.createKeyPair(request);
        return response.getKeyPair();
    }

    public DeleteKeyPairResult deleteKeyPair(String keyName) {
        DeleteKeyPairRequest request = new DeleteKeyPairRequest().withKeyName(keyName);
        return amazonEC2.deleteKeyPair(request);
    }

    public String createSecurityGroup(String groupName, String groupDescription) {

        CreateSecurityGroupRequest createRequest = new CreateSecurityGroupRequest()
                .withGroupName(groupName)
                .withDescription(groupDescription);

        CreateSecurityGroupResult createResponse = amazonEC2.createSecurityGroup(createRequest);
        return createResponse.getGroupId();
    }

    public String createSecurityGroup(String groupName, String groupDescription, String vpcId) {

        CreateSecurityGroupRequest createRequest = new CreateSecurityGroupRequest()
                                                        .withGroupName(groupName)
                                                        .withDescription(groupDescription)
                                                        .withVpcId(vpcId);

        CreateSecurityGroupResult createResponse = amazonEC2.createSecurityGroup(createRequest);
        return createResponse.getGroupId();
    }

    public DeleteSecurityGroupResult deleteSecurityGroup(String groupName) {

        DeleteSecurityGroupRequest request = new DeleteSecurityGroupRequest()
                                                    .withGroupName(groupName);
        return amazonEC2.deleteSecurityGroup(request);
    }

    public AuthorizeSecurityGroupIngressResult authorizeSecurityGroup(String groupName, ProtocolFamily protocolFamily, String ipAddress,
                                                                      InternetProtocol ipProtocol, int fromPort, int toPort) {

        IpPermission ipPerm = new IpPermission()
                .withIpProtocol(ipProtocol.value())
                .withToPort(fromPort)
                .withFromPort(toPort);

        if(protocolFamily == ProtocolFamily.IPV4) {
            IpRange ipRange = new IpRange().withCidrIp(ipAddress);
            ipPerm.withIpv4Ranges(ipRange);
        } else if(protocolFamily == ProtocolFamily.IPV6) {
            Ipv6Range ipv6Range = new Ipv6Range().withCidrIpv6(ipAddress);
            ipPerm.withIpv6Ranges(ipv6Range);
        }

        AuthorizeSecurityGroupIngressRequest auth_request = new AuthorizeSecurityGroupIngressRequest()
                .withGroupName(groupName)
                .withIpPermissions(ipPerm);

        return amazonEC2.authorizeSecurityGroupIngress(auth_request);
    }

    public AuthorizeSecurityGroupIngressResult authorizeSecurityGroup(String groupName, ProtocolFamily protocolFamily, List<String> ipAddresses,
                                                                      InternetProtocol ipProtocol, int fromPort, int toPort) {

        IpPermission ipPerm = new IpPermission()
                .withIpProtocol(ipProtocol.value())
                .withToPort(fromPort)
                .withFromPort(toPort);

            if(protocolFamily == ProtocolFamily.IPV4) {
                List<IpRange> ipRanges = ipAddresses.stream().map(ipAddress -> new IpRange().withCidrIp(ipAddress))
                                                             .collect(Collectors.toList());
                ipPerm.withIpv4Ranges(ipRanges);
            } else if(protocolFamily == ProtocolFamily.IPV6) {
                List<Ipv6Range> ipv6Ranges = ipAddresses.stream().map(ipAddress -> new Ipv6Range().withCidrIpv6(ipAddress))
                        .collect(Collectors.toList());
                ipPerm.withIpv6Ranges(ipv6Ranges);
            }

        AuthorizeSecurityGroupIngressRequest auth_request = new AuthorizeSecurityGroupIngressRequest()
                                                                .withGroupName(groupName)
                                                                .withIpPermissions(ipPerm);

        return amazonEC2.authorizeSecurityGroupIngress(auth_request);
    }

    public List<Address> getAddresses() {
        return amazonEC2.describeAddresses().getAddresses();
    }

    public List<Instance> getInstances(String... instanceIds) {

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        if (instanceIds != null) {
            request.withInstanceIds(Arrays.asList(instanceIds));
        }

        DescribeInstancesResult response = amazonEC2.describeInstances(request);

        return response.getReservations().stream().map(Reservation::getInstances)
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    public String getVpcId(String instanceId) {

        String vpcId = null;
        List<Instance> instances = getInstances(instanceId);

        if(!CollectionUtils.isEmpty(instances)) {
            vpcId = instances.get(0).getVpcId();
        }

        return vpcId;
    }

    public List<Region> getRegions() {
        DescribeRegionsResult regionsResponse = amazonEC2.describeRegions();
        return regionsResponse.getRegions();
    }

    public List<AvailabilityZone> getAvailabilityZones() {
        DescribeAvailabilityZonesResult availabilityZonesResponse = amazonEC2.describeAvailabilityZones();
        return availabilityZonesResponse.getAvailabilityZones();
    }

    public List<KeyPairInfo> getKeyPairs() {
        return amazonEC2.describeKeyPairs().getKeyPairs();
    }

    public List<SecurityGroup> getSecurityGroups() {
        return amazonEC2.describeSecurityGroups().getSecurityGroups();
    }

    public List<SecurityGroup> getSecurityGroups(String... groupNames) {
        DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest()
                                                    .withGroupNames(groupNames);
        return amazonEC2.describeSecurityGroups(request).getSecurityGroups();
    }

    public List<Image> getImages(String... imageIds) {
        DescribeImagesRequest request = new DescribeImagesRequest();
        request.setImageIds(Arrays.asList(imageIds));
        DescribeImagesResult result = amazonEC2.describeImages(request);
        return result.getImages();
    }

    public List<Volume> getVolumes(String... volumeIds) {
        DescribeVolumesRequest request = new DescribeVolumesRequest();
        request.setVolumeIds(Arrays.asList(volumeIds));
        DescribeVolumesResult result = amazonEC2.describeVolumes(request);
        return result.getVolumes();
    }

    public List<Snapshot> getSnapshots(String... snapshotIds) {
        DescribeSnapshotsRequest request = new DescribeSnapshotsRequest();
        request.withOwnerIds(Arrays.<String>asList("self"));
        request.setSnapshotIds(Arrays.asList(snapshotIds));
        DescribeSnapshotsResult result = amazonEC2.describeSnapshots(request);
        return result.getSnapshots();
    }

    public ReleaseAddressResult releaseAddress(String allocationId) {
        ReleaseAddressRequest request = new ReleaseAddressRequest()
                                            .withAllocationId(allocationId);
        return amazonEC2.releaseAddress(request);
    }

    public StartInstancesResult startInstances(String... instanceIds) {
        StartInstancesRequest request = new StartInstancesRequest()
                                                .withInstanceIds(instanceIds);
        return amazonEC2.startInstances(request);
    }

    public StopInstancesResult stopInstances(String... instanceIds) {
        StopInstancesRequest request = new StopInstancesRequest()
                                                .withInstanceIds(instanceIds);
        return amazonEC2.stopInstances(request);
    }

    public RebootInstancesResult rebootInstances(String... instanceIds) {
        RebootInstancesRequest request = new RebootInstancesRequest()
                                                .withInstanceIds(instanceIds);
        return amazonEC2.rebootInstances(request);
    }

    public MonitorInstancesResult monitorInstances(String... instanceIds) {
        MonitorInstancesRequest request = new MonitorInstancesRequest()
                                                .withInstanceIds(instanceIds);
        return amazonEC2.monitorInstances(request);
    }

    public UnmonitorInstancesResult unmonitorInstances(String... instanceIds) {
        UnmonitorInstancesRequest request = new UnmonitorInstancesRequest()
                                                .withInstanceIds(instanceIds);
        return amazonEC2.unmonitorInstances(request);
    }

    public List<String> requestSpotInstances(String imageId, InstanceType instanceType, String keyName, String groupName,
                                     String spotPrice, int instanceCount) {

        RequestSpotInstancesRequest requestRequest = new RequestSpotInstancesRequest();
        requestRequest.setSpotPrice(spotPrice);
        requestRequest.setInstanceCount(instanceCount);

        LaunchSpecification launchSpecification = new LaunchSpecification();
        launchSpecification.setImageId(imageId);
        launchSpecification.setInstanceType(instanceType);
        launchSpecification.setSecurityGroups(Collections.singletonList(groupName));
        requestRequest.setLaunchSpecification(launchSpecification);

        RequestSpotInstancesResult requestResult = amazonEC2.requestSpotInstances(requestRequest);
        return requestResult.getSpotInstanceRequests()
                            .stream()
                            .map(request -> request.getSpotInstanceRequestId())
                            .collect(Collectors.toList());
    }

    public List<SpotInstanceRequest> getSpotInstanceRequests(List<String> spotInstanceRequestIds) {
        DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
        describeRequest.setSpotInstanceRequestIds(spotInstanceRequestIds);
        DescribeSpotInstanceRequestsResult describeResult = amazonEC2.describeSpotInstanceRequests(describeRequest);
        return describeResult.getSpotInstanceRequests();
    }

    public void cancelSpotInstances(List<String> spotInstanceRequestIds) {
        CancelSpotInstanceRequestsRequest cancelRequest = new CancelSpotInstanceRequestsRequest(spotInstanceRequestIds);
        amazonEC2.cancelSpotInstanceRequests(cancelRequest);
    }
    public void terminateSpotInstances(List<String> instanceIds) {
        TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIds);
        amazonEC2.terminateInstances(terminateRequest);
    }
}
