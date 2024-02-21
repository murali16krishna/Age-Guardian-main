package com.mobile.computing.context.monitoring;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

import java.util.HashMap;
import java.util.Map;

public class ViewGuardianActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_guardian);
        getUserData();
    }

    private void getUserData() {
        String awsAccessKey = "AKIAULVF54ZQ54WJY6FJ";
        String awsSecretKey = "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";
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(awsCreds);

        queryDynamoDB(ddbClient);
    }

    private void queryDynamoDB(AmazonDynamoDBClient ddbClient) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<String, AttributeValue> key = new HashMap<>();
                    key.put("id", new AttributeValue().withS("1"));

                    GetItemRequest request = new GetItemRequest()
                            .withTableName("guardian")
                            .withKey(key);

                    GetItemResult result = ddbClient.getItem(request);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result != null && result.getItem() != null) {
                                Map<String, AttributeValue> item = result.getItem();
                                AttributeValue firstName = item.get("first_name");
                                AttributeValue lastName = item.get("last_name");
                                AttributeValue gender = item.get("sex");
                                AttributeValue age = item.get("age");
                                AttributeValue phoneNumber = item.get("mobile");
                                AttributeValue bloodGroup = item.get("blood_group");

                                TextView firstNameTextField = (TextView) findViewById(R.id.firstNameTextView);
                                TextView lastNameTextField = (TextView) findViewById(R.id.lastNameTextView);
                                TextView genderTextField = (TextView) findViewById(R.id.genderTextView);
                                TextView ageTextField = (TextView) findViewById(R.id.ageTextView);
                                TextView phoneNumberField = (TextView) findViewById(R.id.phoneTextView);
                                TextView bloodGroupTextField = (TextView) findViewById(R.id.bloodGroupTextView);


                                firstNameTextField.setText(firstName.getS());
                                lastNameTextField.setText(lastName.getS());
                                genderTextField.setText(gender.getS());
                                ageTextField.setText(age.getS());
                                phoneNumberField.setText(phoneNumber.getS());
                                bloodGroupTextField.setText(bloodGroup.getS());

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

}