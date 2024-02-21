package com.mobile.computing.context.guardianapp;

import static com.mobile.computing.context.guardianapp.utils.constants.Constants.USER_ID;
import static com.mobile.computing.context.guardianapp.utils.constants.Constants.USER_NAME;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.mobile.computing.context.guardianapp.adapter.SymptomsAdapter;

import java.util.HashMap;
import java.util.Map;

public class SymptomsDisplayActivity extends AppCompatActivity {

    String userId;
    String userName;
    String symptomsDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms_display);

        userId = getIntent().getStringExtra(USER_ID);
        userName = getIntent().getStringExtra(USER_NAME);

        TextView userNameDisplay = findViewById(R.id.userNameSymptoms);
        userNameDisplay.setText(userName);

        TextView symptomsDateInfo = findViewById(R.id.symptomsDateInfo);
        String displayText = "Displayed information is the most recent";
        symptomsDateInfo.setText(displayText);

        RecyclerView recyclerView = findViewById(R.id.symptomsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Map<String, Integer> symptoms = (Map<String, Integer>) getIntent().getSerializableExtra("symptoms");
        SymptomsAdapter adapter = new SymptomsAdapter(symptoms);
        recyclerView.setAdapter(adapter);
    }
}