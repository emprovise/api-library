package com.emprovise.api.amazon.ws.messaging;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.util.List;

/**
 * @see <a href="https://github.com/awsdocs/aws-doc-sdk-examples/tree/master/java/example_code/sqs/src/main/java/aws/example/sqs">SQS Example</a>
 * <a href="http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-sqs.html">Amazon SQS Example</a>
 * <a href="https://github.com/caffinc/alerts/blob/master/Alerts/src/main/java/com/caffinc/alerts/Alerts.java">More SQS Examples</a>
 * <a href="http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-send-message.html">Sending Message using SQS</a>
 * <a href="http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-how-it-works.html">How SQS Works</a>
 * <a href="https://github.com/springuni/springuni-examples/tree/master/springuni-jms-sqs">springuni-jms-sqs</a>
 */
public class AmazonSQSService {

    private AmazonSQS amazonSQS;

    public AmazonSQSService(AWSCredentials credentials, Regions region) {
        this(new AWSStaticCredentialsProvider(credentials), region);
    }

    public AmazonSQSService(AWSCredentials credentials, Regions region, ClientConfiguration config) {
        this(new AWSStaticCredentialsProvider(credentials), region, config);
    }

    public AmazonSQSService(AWSCredentialsProvider awsCredentialsProvider, Regions region) {
        this(awsCredentialsProvider, region, new ClientConfiguration());
    }

    public AmazonSQSService(AWSCredentialsProvider awsCredentialsProvider, Regions region, ClientConfiguration config) {
        this.amazonSQS = AmazonSQSClientBuilder.standard()
                            .withCredentials(awsCredentialsProvider)
                            .withRegion(region)
                            .withClientConfiguration(config)
                            .build();
    }

    public SendMessageResult sendMessage(String queueName, String message) {
        String queueUrl = amazonSQS.getQueueUrl(queueName).getQueueUrl();
        return amazonSQS.sendMessage(new SendMessageRequest(queueUrl, message));
    }

    public SendMessageBatchResult sendMessageBatch(String queueName, String... messages) {

        if(messages == null || messages.length == 0) {
            throw new IllegalArgumentException("Messages are empty");
        }

        String queueUrl = amazonSQS.getQueueUrl(queueName).getQueueUrl();

        SendMessageBatchRequestEntry[] entries = new SendMessageBatchRequestEntry[messages.length];

        for (int i = 0; i < messages.length; i++) {
            entries[i] = new SendMessageBatchRequestEntry("msg_" + i, messages[i]);
        }

        SendMessageBatchRequest send_batch_request = new SendMessageBatchRequest()
                .withQueueUrl(queueUrl)
                .withEntries(entries);
        return amazonSQS.sendMessageBatch(send_batch_request);
    }

    public List<Message> receiveMessage(String queueName) {
        String queueUrl = amazonSQS.getQueueUrl(queueName).getQueueUrl();
        return amazonSQS.receiveMessage(queueUrl).getMessages();
    }

    public void deleteMessage(String queueName, Message message) {
        String queueUrl = amazonSQS.getQueueUrl(queueName).getQueueUrl();
        DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueUrl, message.getReceiptHandle());
        amazonSQS.deleteMessage(deleteMessageRequest);
    }

    /**
     * Creates the queue
     * @return Queue URL of the created queue
     */
    public String createQueue(String queueName) {
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
        return amazonSQS.createQueue(createQueueRequest).getQueueUrl();
    }

    /**
     * Returns the list of queues for this account
     * @return List of Queues
     */
    public List<String> listQueues() {
        return amazonSQS.listQueues().getQueueUrls();
    }

    /**
     * Deletes the queue
     */
    public void deleteQueue(String queueName) {
        String queueUrl = amazonSQS.getQueueUrl(queueName).getQueueUrl();
        amazonSQS.deleteQueue( new DeleteQueueRequest(queueUrl));
    }

    /**
     * Use SQSConnectionFactory is used to create SQSConnection. SQSConnection can then in turn be used to create Session which is used to create
     * Producer or Consumer to send or receive messages from the queue connection respectively.
     *
     * @param queueName
     * @param awsCredentialsProvider
     * @return
     */
    public SQSConnectionFactory getQueueConnectionFactory(String queueName, AWSCredentialsProvider awsCredentialsProvider, Regions region) {
        String queueUrl = amazonSQS.getQueueUrl(queueName).getQueueUrl();
        return getSQSConnectionFactory(queueUrl, awsCredentialsProvider, region);
    }

    private SQSConnectionFactory getSQSConnectionFactory(String sqsEndPoint, AWSCredentialsProvider awsCredentialsProvider, Regions region) {
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(sqsEndPoint, region.getName());
        return new SQSConnectionFactory(
                new ProviderConfiguration(),
                        AmazonSQSClientBuilder.standard()
                                .withRegion(region.getName())
                                .withCredentials(awsCredentialsProvider)
                                .withEndpointConfiguration(endpointConfiguration)
        );
    }
}
