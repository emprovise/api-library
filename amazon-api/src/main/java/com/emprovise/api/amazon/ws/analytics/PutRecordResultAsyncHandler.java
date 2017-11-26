package com.emprovise.api.amazon.ws.analytics;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest;
import com.amazonaws.services.kinesisfirehose.model.PutRecordResult;

public class PutRecordResultAsyncHandler implements AsyncHandler<PutRecordRequest, PutRecordResult> {

    private AmazonFirehoseAsyncService amazonFirehoseAsyncService;
    private PutRecordRequest putRecordRequest;
    private static final double MAX_RETRY = 5;
    private static final long THIRTY_SECONDS = 30000;

    public PutRecordResultAsyncHandler(AmazonFirehoseAsyncService amazonFirehoseAsyncService, PutRecordRequest putRecordRequest) {
        this.amazonFirehoseAsyncService = amazonFirehoseAsyncService;
        this.putRecordRequest = putRecordRequest;
    }

    @Override
    public void onError(Exception exception) {
        int retries = 1;
        while (retries <= MAX_RETRY) {
            try {
                amazonFirehoseAsyncService.putRecordAsync(putRecordRequest);
                return;
            } catch (Exception ex) {
                ++retries;
                try {
                    Thread.sleep(THIRTY_SECONDS);
                } catch (InterruptedException iex) {
                }
            }
        }
        exception.printStackTrace();
    }

    @Override
    public void onSuccess(PutRecordRequest request, PutRecordResult putRecordResult) {
    }
 }
