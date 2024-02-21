package com.mobile.computing.context.guardianapp.service;

import static com.mobile.computing.context.guardianapp.utils.constants.AWSConfig.awsAccessKey;
import static com.mobile.computing.context.guardianapp.utils.constants.AWSConfig.awsSecretKey;
import static com.mobile.computing.context.guardianapp.utils.constants.AWSConfig.responseQueueUrl;

import android.os.Handler;
import android.os.Looper;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;

import java.util.List;

public class SQSPoller {

    private AmazonSQS sqsClient;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPolling = false;

    public SQSPoller() {
        AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

        sqsClient = new AmazonSQSClient(credentials);
    }

    public void startPolling(final SQSListener listener) {
        isPolling = true;

        ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
                .withQueueUrl(responseQueueUrl)
                .withWaitTimeSeconds(2);

        new Thread(() -> {
            while (isPolling) {
                try {
                    List<Message> messages = sqsClient.receiveMessage(receiveRequest).getMessages();

                    for (Message message : messages) {
                        String requestContent = message.getBody();

                        handler.post(() -> listener.onMessageReceived(requestContent));

                        sqsClient.deleteMessage(responseQueueUrl, message.getReceiptHandle());
                    }
                } catch (AmazonClientException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stopPolling() {
        isPolling = false;
    }

    public interface SQSListener {
        void onMessageReceived(String message);
    }
}
