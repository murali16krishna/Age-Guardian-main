package com.mobile.computing.context.monitoring.utils;


import android.os.AsyncTask;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class SaveSymptoms extends AsyncTask<Map, Void, Void> {

    BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAULVF54ZQ54WJY6FJ", "GBo58f/F4hojsUQKQp0Do0FlcjO2pefHugqdmGrj");
    AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(awsCreds);

    @Override
    protected Void doInBackground(Map... params) {
        // Initialize the AmazonDynamoDBClient

        // Save the data to DynamoDB
        Map<String, AttributeValue> putMap = new HashMap<>();
        putMap.put("user_id", new AttributeValue("1"));
        Map<String, String> param = params[0];
        for (Map.Entry<String, String> entry : param.entrySet())
            putMap.put(entry.getKey(), new AttributeValue(entry.getValue()));
        PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName("user_symptoms")
                .withItem(putMap)
                .withReturnValues(ReturnValue.ALL_OLD);

        System.out.println("putItemRequest = " + putItemRequest);

        ddbClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        PutItemResult putItemResult = ddbClient.putItem(putItemRequest);
        System.out.println("putItemResult = " + putItemResult);

        return null;
    }
}
