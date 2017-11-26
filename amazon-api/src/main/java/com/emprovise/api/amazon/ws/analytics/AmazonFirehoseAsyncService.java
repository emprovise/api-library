package com.emprovise.api.amazon.ws.analytics;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsync;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsyncClientBuilder;
import com.amazonaws.services.kinesisfirehose.model.*;

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

/**
 * This is a paid service and not available under Amazon free tier
 */
public class AmazonFirehoseAsyncService {

    private AmazonKinesisFirehoseAsync amazonKinesisFirehoseAsync;

    public AmazonFirehoseAsyncService(AWSCredentials credentials, Regions region) {
        this(new AWSStaticCredentialsProvider(credentials), region);
    }

    public AmazonFirehoseAsyncService(AWSCredentials credentials, Regions region, ClientConfiguration config) {
        this(new AWSStaticCredentialsProvider(credentials), region, config);
    }

    public AmazonFirehoseAsyncService(AWSCredentialsProvider awsCredentialsProvider, Regions region) {
        this(awsCredentialsProvider, region, new ClientConfiguration());
    }

    public AmazonFirehoseAsyncService(AWSCredentialsProvider awsCredentialsProvider, Regions region, ClientConfiguration config) {
        this.amazonKinesisFirehoseAsync = AmazonKinesisFirehoseAsyncClientBuilder.standard()
                                    .withCredentials(awsCredentialsProvider)
                                    .withRegion(region)
                                    .withClientConfiguration(config)
                                    .withExecutorFactory(() -> Executors.newFixedThreadPool(10))
                                    .build();
    }

    public void putRecordAsync(String firehoseStream, String record) {
        PutRecordRequest putRecord = getPutRecordRequest(firehoseStream, record);
        putRecordAsync(putRecord);
    }

    public void putRecordAsync(PutRecordRequest putRecordRequest) {
        amazonKinesisFirehoseAsync.putRecordAsync(putRecordRequest, new PutRecordResultAsyncHandler(this, putRecordRequest));
    }

    private PutRecordRequest getPutRecordRequest(String firehoseStream, String record) {
        return new PutRecordRequest().withDeliveryStreamName(firehoseStream)
                                     .withRecord(new Record().withData(ByteBuffer.wrap(record.getBytes())));
    }

    public DeliveryStreamDescription describeDeliveryStream(String deliveryStreamName) {
        DescribeDeliveryStreamRequest describeDeliveryStreamRequest = new DescribeDeliveryStreamRequest();
        describeDeliveryStreamRequest.withDeliveryStreamName(deliveryStreamName);
        DescribeDeliveryStreamResult streamResponse = amazonKinesisFirehoseAsync.describeDeliveryStream(describeDeliveryStreamRequest);
        return streamResponse.getDeliveryStreamDescription();
    }

    public String getStreamStatus(String deliveryStreamName) {
        DeliveryStreamDescription deliveryStreamDescription = describeDeliveryStream(deliveryStreamName);
        return deliveryStreamDescription.getDeliveryStreamStatus();
    }

    public DeleteDeliveryStreamResult deleteStream(String firehoseStream) {
        DeleteDeliveryStreamRequest request = new DeleteDeliveryStreamRequest();
        request.withDeliveryStreamName(firehoseStream);
        return amazonKinesisFirehoseAsync.deleteDeliveryStream(request);
    }
}
