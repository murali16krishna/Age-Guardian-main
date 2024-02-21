package com.mobile.computing.context.guardianapp;

import static com.mobile.computing.context.guardianapp.utils.constants.AWSConfig.awsAccessKey;
import static com.mobile.computing.context.guardianapp.utils.constants.AWSConfig.awsSecretKey;
import static com.mobile.computing.context.guardianapp.utils.constants.Constants.USER_ID;
import static com.mobile.computing.context.guardianapp.utils.constants.Constants.USER_NAME;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layout = findViewById(R.id.userButtonContainer);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getUserInfo().thenAccept(firstNames -> {
                runOnUiThread(() -> {
                    for (String name : firstNames) {
                        Button button = new Button(this);
                        button.setText(name);
                        button.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_700));
                        button.setTextColor(ContextCompat.getColor(this, R.color.white));

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                800, 200
                        );
                        params.setMargins(0, 50, 0, 50);
                        button.setLayoutParams(params);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openUserDetail(name, "1");
                            }
                        });
                        layout.addView(button);
                    }
                });
            });
        }
    }

    private CompletableFuture<List<String>> getUserInfo() {
        CompletableFuture<List<String>> future = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            future = new CompletableFuture<>();
        }

        CompletableFuture<List<String>> finalFuture = future;
        new Thread(() -> {
            try {
                List<String> firstNames = queryDynamoDBForUserInfo();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    finalFuture.complete(firstNames);
                }
            } catch (Exception e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    finalFuture.completeExceptionally(e);
                }
            }
        }).start();

        return finalFuture;
    }

    private List<String> queryDynamoDBForUserInfo() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(awsCreds);
        List<String> firstNames = new ArrayList<>();

        try {
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName("user");
            ScanResult result = ddbClient.scan(scanRequest);
            for (Map<String, AttributeValue> item : result.getItems()) {
                AttributeValue firstNameAttributeValue = item.get("first_name");
                if (firstNameAttributeValue != null) {
                    firstNames.add(firstNameAttributeValue.getS());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return firstNames;

    }

    private void openUserDetail(String userName, String userId) {
        Intent intent = new Intent(MainActivity.this, UserDetailActivity.class);
        intent.putExtra(USER_NAME, userName);
        intent.putExtra(USER_ID, userId);
        startActivity(intent);
    }
}

