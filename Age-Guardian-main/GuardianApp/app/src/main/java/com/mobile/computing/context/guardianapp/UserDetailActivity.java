package com.mobile.computing.context.guardianapp;

import static com.mobile.computing.context.guardianapp.utils.constants.Constants.USER_ID;
import static com.mobile.computing.context.guardianapp.utils.constants.Constants.USER_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserDetailActivity extends AppCompatActivity {

    TextView heartRateValue;
    TextView respiratoryRateValue;
    Button buttonHeartRate;
    Button buttonRespiratoryRate;
    String userId;
    String userName;

    HashMap<String,Integer> symptoms = new HashMap<>();

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }


    public void setRespiratoryRate(int respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    int heartRate;
    int respiratoryRate;

    private static final String requestQueueUrl = "https://sqs.us-east-2.amazonaws.com/299921958497/userDetailsRequestQueue";
    private static final String awsAccessKey = "AKIAULVF54ZQ54WJY6FJ";
    private static final String awsSecretKey = "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";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        heartRateValue = findViewById(R.id.heartRateValue);
        respiratoryRateValue = findViewById(R.id.respiratoryRateValue);
        buttonHeartRate = findViewById(R.id.buttonHeartRate);
        buttonRespiratoryRate = findViewById(R.id.buttonRespiratoryRate);

        userId = getIntent().getStringExtra(USER_ID);
        userName = getIntent().getStringExtra(USER_NAME);

        TextView userNameDisplay = findViewById(R.id.userName);
        userNameDisplay.setText(userName);

        getUserData();

        buttonHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateHeartRate();
            }
        });

        buttonRespiratoryRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateRespiratoryRate();
            }
        });

        Button viewSymptomsButton = findViewById(R.id.buttonViewSymptoms);
        viewSymptomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserData();
                openSymptomsActivity();
            }
        });

        Button locateButton = findViewById(R.id.buttonLocate);
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocateActivity();
            }
        });
    }

    private void getUserData() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(awsCreds);

        queryDynamoDB(ddbClient);
    }

    private void updateHeartRate() {
        heartRateValue.setText(new StringBuilder().append(heartRate).append(" bpm").toString());
    }

    private void queryDynamoDB(AmazonDynamoDBClient ddbClient) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<String, AttributeValue> key = new HashMap<>();
                    key.put("user_id", new AttributeValue().withS("1"));

                    GetItemRequest request = new GetItemRequest()
                            .withTableName("user_symptoms")
                            .withKey(key);

                    GetItemResult result = ddbClient.getItem(request);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result != null && result.getItem() != null) {
                                Map<String, AttributeValue> item = result.getItem();
                                AttributeValue respiratory_rate = item.get("respiratory_rate");
                                AttributeValue heart_rate = item.get("heart_rate");

                                symptoms.put("COUGH",Integer.valueOf(item.get("cough").getS()));
                                symptoms.put("DIARRHEA",Integer.valueOf(item.get("diarrhea").getS()));
                                symptoms.put("FEELING TIRED",Integer.valueOf(item.get("feelingTired").getS()));
                                symptoms.put("FEVER",Integer.valueOf(item.get("fever").getS()));
                                symptoms.put("HEADACHE",Integer.valueOf(item.get("headache").getS()));
                                symptoms.put("LOSS OF SMELL AND TASTE",Integer.valueOf(item.get("lossOfSmellOrTaste").getS()));
                                symptoms.put("MUSCLE ACHE",Integer.valueOf(item.get("muscleAche").getS()));
                                symptoms.put("NAUSEA",Integer.valueOf(item.get("nausea").getS()));
                                symptoms.put("SHORTNESS OF BREATH",Integer.valueOf(item.get("shortnessOfBreath").getS()));
                                symptoms.put("SOAR THROAT",Integer.valueOf(item.get("soarThroat").getS()));


                                setHeartRate(Integer.valueOf(heart_rate.getS()));
                                setRespiratoryRate(Integer.valueOf(respiratory_rate.getS()));
                            } else {

                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateRespiratoryRate() {
        respiratoryRateValue.setText(new StringBuilder().append(respiratoryRate).append(" breaths/min").toString());
    }

    private void openSymptomsActivity() {
        Intent intent = new Intent(UserDetailActivity.this, SymptomsDisplayActivity.class);
        intent.putExtra(USER_ID, userId);
        intent.putExtra(USER_NAME, userName);
        intent.putExtra("symptoms", (Serializable) symptoms);
        startActivity(intent);
    }

    private void openLocateActivity() {
        Intent intent = new Intent(UserDetailActivity.this, LocateActivity.class);
        intent.putExtra(USER_ID, userId);
        intent.putExtra(USER_NAME, userName);
        startActivity(intent);
    }

    private void pushToRequestQueue(QueueMessageBody queueMessageBody) {
        AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        AmazonSQSClient sqs = new AmazonSQSClient(credentials);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Gson gson = new Gson();
                    String body = gson.toJson(queueMessageBody);
                    SendMessageRequest sendMsgRequest = new SendMessageRequest()
                            .withQueueUrl(requestQueueUrl)
                            .withMessageBody(body);
                    sqs.sendMessage(sendMsgRequest);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
