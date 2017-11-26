package com.emprovise.api.amazon.ws.analytics;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehose;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClientBuilder;
import com.amazonaws.services.kinesisfirehose.model.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a paid service and not available under Amazon free tier
 */
public class AmazonFirehoseService {

    private AmazonKinesisFirehose amazonKinesisFirehose;

    public AmazonFirehoseService(AWSCredentials credentials, Regions region) {
        this(new AWSStaticCredentialsProvider(credentials), region);
    }

    public AmazonFirehoseService(AWSCredentials credentials, Regions region, ClientConfiguration config) {
        this(new AWSStaticCredentialsProvider(credentials), region, config);
    }

    public AmazonFirehoseService(AWSCredentialsProvider awsCredentialsProvider, Regions region) {
        this(awsCredentialsProvider, region, new ClientConfiguration());
    }

    public AmazonFirehoseService(AWSCredentialsProvider awsCredentialsProvider, Regions region, ClientConfiguration config) {
        this.amazonKinesisFirehose = AmazonKinesisFirehoseClientBuilder.standard()
                .withCredentials(awsCredentialsProvider)
                .withRegion(region)
                .withClientConfiguration(config)
                .build();
    }

    public PutRecordResult putRecord(String deliveryStreamName, String data) {
        PutRecordRequest putRecordRequest = new PutRecordRequest();
        putRecordRequest.setDeliveryStreamName(deliveryStreamName);
        putRecordRequest.setRecord(createRecord(data));
        return amazonKinesisFirehose.putRecord(putRecordRequest);
    }

    public PutRecordBatchResult putRecordBatch(String deliveryStreamName, List<String> dataList) {

        List<Record> recordList = dataList.stream()
                                            .map(entry -> createRecord(entry))
                                            .collect(Collectors.toList());

        PutRecordBatchRequest putRecordBatchRequest = new PutRecordBatchRequest();
        putRecordBatchRequest.setDeliveryStreamName(deliveryStreamName);
        putRecordBatchRequest.setRecords(recordList);
        return amazonKinesisFirehose.putRecordBatch(putRecordBatchRequest);
    }

    public List<String> listDeliveryStreams() {
        ListDeliveryStreamsRequest deliveryStreamsRequest = new ListDeliveryStreamsRequest();
        ListDeliveryStreamsResult deliveryStreamsResult = amazonKinesisFirehose.listDeliveryStreams(deliveryStreamsRequest);
        List<String> deliveryStreamNames = deliveryStreamsResult.getDeliveryStreamNames();
        while (deliveryStreamsResult.isHasMoreDeliveryStreams()) {
            if (!deliveryStreamNames.isEmpty()) {
                deliveryStreamsRequest.setExclusiveStartDeliveryStreamName(deliveryStreamNames.get(deliveryStreamNames.size() - 1));
            }

            deliveryStreamsResult = amazonKinesisFirehose.listDeliveryStreams(deliveryStreamsRequest);
            deliveryStreamNames.addAll(deliveryStreamsResult.getDeliveryStreamNames());
        }
        return deliveryStreamNames;
    }

    public DeliveryStreamDescription describeDeliveryStream(String deliveryStreamName) {
        DescribeDeliveryStreamRequest describeDeliveryStreamRequest = new DescribeDeliveryStreamRequest();
        describeDeliveryStreamRequest.withDeliveryStreamName(deliveryStreamName);
        DescribeDeliveryStreamResult streamResponse = amazonKinesisFirehose.describeDeliveryStream(describeDeliveryStreamRequest);
        return streamResponse.getDeliveryStreamDescription();
    }

    public String getStreamStatus(String deliveryStreamName) {
        DeliveryStreamDescription deliveryStreamDescription = describeDeliveryStream(deliveryStreamName);
        return deliveryStreamDescription.getDeliveryStreamStatus();
    }

    public CreateDeliveryStreamResult createRedshiftStream(String deliveryStreamName,
                                                           RedshiftDestinationConfiguration redshiftDestConfig) {
        List<String> deliveryStreamNames = listDeliveryStreams();
        if (deliveryStreamNames != null && deliveryStreamNames.contains(deliveryStreamName)) {
            throw new IllegalArgumentException(String.format("Delivery Stream %s exists", deliveryStreamName));
        }

        CreateDeliveryStreamRequest createDeliveryStreamRequest = new CreateDeliveryStreamRequest();
        createDeliveryStreamRequest.setDeliveryStreamName(deliveryStreamName);
        createDeliveryStreamRequest.setRedshiftDestinationConfiguration(redshiftDestConfig);
        return amazonKinesisFirehose.createDeliveryStream(createDeliveryStreamRequest);
    }

    public void updateRedshiftStream(String deliveryStreamName, String dataTableName, String copyOptions) {
        DeliveryStreamDescription deliveryStreamDescription = describeDeliveryStream(deliveryStreamName);
        UpdateDestinationRequest updateDestinationRequest = new UpdateDestinationRequest()
                                                                .withDeliveryStreamName(deliveryStreamName)
                                                                .withCurrentDeliveryStreamVersionId(deliveryStreamDescription.getVersionId())
                                                                .withDestinationId(deliveryStreamDescription.getDestinations().get(0).getDestinationId());

        CopyCommand updatedCopyCommand = new CopyCommand()
                                                .withDataTableName(dataTableName)
                                                .withCopyOptions(copyOptions);
        RedshiftDestinationUpdate redshiftDestinationUpdate = new RedshiftDestinationUpdate()
                                                                    .withCopyCommand(updatedCopyCommand);

        updateDestinationRequest.setRedshiftDestinationUpdate(redshiftDestinationUpdate);
        amazonKinesisFirehose.updateDestination(updateDestinationRequest);
    }

    private Record createRecord(String data) {
        return new Record().withData(ByteBuffer.wrap((data + "\n").getBytes()));
    }

    public DeleteDeliveryStreamResult deleteStream(String deliveryStreamName) {
        DeleteDeliveryStreamRequest request = new DeleteDeliveryStreamRequest();
        request.withDeliveryStreamName(deliveryStreamName);
        return amazonKinesisFirehose.deleteDeliveryStream(request);
    }
}
