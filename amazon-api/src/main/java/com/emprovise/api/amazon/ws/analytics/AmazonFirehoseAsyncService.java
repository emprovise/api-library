package com.emprovise.api.amazon.ws.analytics;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsync;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsyncClientBuilder;
import com.amazonaws.services.kinesisfirehose.model.*;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This is a paid service and not available under Amazon free tier
 */
public class AmazonFirehoseAsyncService {

    private AmazonKinesisFirehoseAsync amazonKinesisFirehoseAsync;
    private BlockingQueue<Runnable> taskBuffer;
    private ThreadPoolExecutor threadPoolExecutor;

    public static final int DEFAULT_MAX_RETRY_COUNT = 3;
    public static final int DEFAULT_BUFFER_SIZE = 2000;
    private static final int MAX_POOL_SIZE= 50;
    private static final int CORE_POOL_SIZE = 5;
    public static final int DEFAULT_SHUTDOWN_TIMEOUT_SEC = 30;
    public static final int DEFAULT_THREAD_KEEP_ALIVE_SEC = 30;


    public AmazonFirehoseAsyncService(AWSCredentials credentials, Regions region) {
        this(new AWSStaticCredentialsProvider(credentials), region);
    }

    public AmazonFirehoseAsyncService(AWSCredentials credentials, Regions region, ClientConfiguration config) {
        this(new AWSStaticCredentialsProvider(credentials), region, config);
    }

    public AmazonFirehoseAsyncService(AWSCredentialsProvider awsCredentialsProvider, Regions region) {
        this(awsCredentialsProvider, region, getClientConfiguration());
    }

    public AmazonFirehoseAsyncService(AWSCredentialsProvider awsCredentialsProvider, Regions region, ClientConfiguration config) {

        taskBuffer = new LinkedBlockingDeque<>(DEFAULT_BUFFER_SIZE);
        threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, DEFAULT_THREAD_KEEP_ALIVE_SEC, TimeUnit.SECONDS, taskBuffer);
        threadPoolExecutor.prestartAllCoreThreads();

        this.amazonKinesisFirehoseAsync = AmazonKinesisFirehoseAsyncClientBuilder.standard()
                                    .withCredentials(awsCredentialsProvider)
                                    .withRegion(region)
                                    .withClientConfiguration(config)
                                    .withExecutorFactory(() -> threadPoolExecutor)
                                    .build();
    }

    private static ClientConfiguration getClientConfiguration() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setMaxErrorRetry(DEFAULT_MAX_RETRY_COUNT);
        clientConfiguration.setRetryPolicy(new RetryPolicy(PredefinedRetryPolicies.DEFAULT_RETRY_CONDITION,
                                                           PredefinedRetryPolicies.DEFAULT_BACKOFF_STRATEGY,
                                                           DEFAULT_MAX_RETRY_COUNT, true));
        return clientConfiguration;
    }

    /**
     * Returns count of tasks scheduled to send records to Kinesis. Since
     * currently each task maps to sending one record, it is equivalent to number
     * of records in the buffer scheduled to be sent to Kinesis.
     *
     * @return count of tasks scheduled to send records to Kinesis.
     */
    public int getTaskBufferSize() {
        int size = 0;
        if (taskBuffer != null) {
            size = taskBuffer.size();
        }
        return size;
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

    public boolean isStreamActive(String deliveryStreamName) {
        DeliveryStreamDescription deliveryStreamDescription = describeDeliveryStream(deliveryStreamName);
        return DeliveryStreamStatus.ACTIVE.name().equals(deliveryStreamDescription.getDeliveryStreamStatus());
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
