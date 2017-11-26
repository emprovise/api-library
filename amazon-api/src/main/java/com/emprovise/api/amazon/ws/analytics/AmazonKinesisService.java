package com.emprovise.api.amazon.ws.analytics;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a paid service and not available under Amazon free tier
 * @see <a href="http://docs.aws.amazon.com/streams/latest/dev/kinesis-using-sdk-java-create-stream.html">Creating a Stream</a>
 */
public class AmazonKinesisService {

    private AmazonKinesis amazonKinesis;

    public AmazonKinesisService(AWSCredentials credentials, Regions region) {
        this(new AWSStaticCredentialsProvider(credentials), region);
    }

    public AmazonKinesisService(AWSCredentials credentials, Regions region, ClientConfiguration config) {
        this(new AWSStaticCredentialsProvider(credentials), region, config);
    }

    public AmazonKinesisService(AWSCredentialsProvider awsCredentialsProvider, Regions region) {
        this(awsCredentialsProvider, region, new ClientConfiguration());
    }

    public AmazonKinesisService(AWSCredentialsProvider awsCredentialsProvider, Regions region, ClientConfiguration config) {
        this.amazonKinesis = AmazonKinesisClientBuilder.standard()
                .withCredentials(awsCredentialsProvider)
                .withRegion(region)
                .withClientConfiguration(config)
                .build();
    }

    public CreateStreamResult createStream(String streamName, Integer streamSize) {
        CreateStreamRequest createStreamRequest = new CreateStreamRequest();
        createStreamRequest.setStreamName(streamName);
        createStreamRequest.setShardCount(streamSize);
        return amazonKinesis.createStream(createStreamRequest);
    }

    public DescribeStreamResult describeStream(String streamName) {
        DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest();
        describeStreamRequest.setStreamName(streamName);
        return amazonKinesis.describeStream(describeStreamRequest);
    }

    public String getStreamStatus(String streamName) {
        DescribeStreamResult describeStreamResponse = describeStream(streamName);
        return describeStreamResponse.getStreamDescription().getStreamStatus();
    }

    public void deleteStream(String streamName) {
        amazonKinesis.deleteStream(streamName);
    }

    public List<String> getAllStreams() {

        List<String> streamNames = new ArrayList<>();
        ListStreamsRequest listStreamsRequest = new ListStreamsRequest();
        ListStreamsResult listStreamsResult = amazonKinesis.listStreams(listStreamsRequest);

        while (listStreamsResult.getHasMoreStreams())
        {
            if (streamNames.size() > 0) {
                listStreamsRequest.setExclusiveStartStreamName(streamNames.get(streamNames.size() - 1));
            }
            listStreamsResult = amazonKinesis.listStreams(listStreamsRequest);
            streamNames.addAll(listStreamsResult.getStreamNames());
        }

        return listStreamsResult.getStreamNames();
    }
}
