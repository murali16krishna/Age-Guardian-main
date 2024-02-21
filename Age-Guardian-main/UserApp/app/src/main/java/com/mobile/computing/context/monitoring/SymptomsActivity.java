package com.mobile.computing.context.monitoring;

import static com.mobile.computing.context.monitoring.utils.Constants.HEART_RATE;
import static com.mobile.computing.context.monitoring.utils.Constants.RESPIRATORY_RATE;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobile.computing.context.monitoring.adapter.SymptomDetailAdapter;
import com.mobile.computing.context.monitoring.contentProvider.MonitoringDB;
import com.mobile.computing.context.monitoring.entities.SymptomDetail;
import com.mobile.computing.context.monitoring.utils.Constants;
import com.mobile.computing.context.monitoring.utils.SaveSymptoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SymptomsActivity extends AppCompatActivity {


    public static final String NAUSEA = "Nausea";;
    public static final String HEADACHE = "Headache";
    public static final String DIARRHEA = "Diarrhea";
    public static final String SOAR_THROAT = "Soar Throat";
    public static final String FEVER = "Fever";
    public static final String MUSCLE_ACHE = "Muscle Ache";
    public static final String LOSS_OF_SMELL_OR_TASTE = "Loss of Smell/Taste";
    public static final String COUGH = "Cough";
    public static final String SHORTNESS_OF_BREATH = "Shortness of Breath";
    public static final String FEELING_TIRED = "Feeling Tired";


    public static List<String> symptomsList = List.of(NAUSEA, HEADACHE, DIARRHEA,
                                                      SOAR_THROAT, FEVER, MUSCLE_ACHE,
                                                      LOSS_OF_SMELL_OR_TASTE, COUGH,
                                                      SHORTNESS_OF_BREATH, FEELING_TIRED);

    private LinkedHashMap<String, Integer> symptomsSeverityMap;
    List<Integer> symptomSeverity = null;
    List<SymptomDetail> symptomDetails = null;

    MonitoringDB monitoringDB;

    Integer heartRate = 0;
    Integer respiratoryRate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        symptomSeverity = new ArrayList<>();

        for(int i = 0; i< symptomsList.size();i++){
            symptomSeverity.add(0);
        }

        monitoringDB = new MonitoringDB(SymptomsActivity.this);
        symptomsSeverityMap = new LinkedHashMap<>();

        symptomDetails = new ArrayList<>();

        for(String symptom : symptomsList){
            symptomDetails.add(new SymptomDetail(symptom, 0));
            symptomsSeverityMap.put(symptom, 0);
        }

        heartRate = Integer.valueOf(getIntent().getSerializableExtra(HEART_RATE).toString());
        respiratoryRate = Integer.valueOf(getIntent().getSerializableExtra(RESPIRATORY_RATE).toString());

        SymptomDetailAdapter symptomDetailAdapter = new SymptomDetailAdapter(this, symptomDetails);

        ListView listView = (ListView) findViewById(R.id.listview_symptom);
        listView.setAdapter(symptomDetailAdapter);
    }

    public void decreaseSeverity(View view) {

        LinearLayout parentRow = (LinearLayout) view.getParent();

        TextView severityView = (TextView) parentRow.findViewById(R.id.symptom_severity);
        String quantityString = severityView.getText().toString();
        int symptomNumber = Integer.parseInt(quantityString);
        symptomNumber -= 1;

        if (symptomNumber < 0) {
            symptomNumber = 0;
            Toast.makeText(SymptomsActivity.this, "Can not be less than 0",
                    Toast.LENGTH_SHORT).show();}

        ListView listView = (ListView) parentRow.getParent();
        final int position = listView.getPositionForView(parentRow);
        symptomSeverity.set(position, symptomNumber);
        symptomDetails.get(position).setSeverity(symptomNumber);
        severityView.setText(String.valueOf(symptomNumber));
    }

    public void increaseSeverity(View view) {

        LinearLayout parentRow = (LinearLayout) view.getParent();

        TextView severityView = (TextView) parentRow.findViewById(R.id.symptom_severity);
        String quantityString = severityView.getText().toString();
        int symptomNumber = Integer.parseInt(quantityString);
        symptomNumber += 1;
        if (symptomNumber > 5) {
            symptomNumber = 5;
            Toast.makeText(SymptomsActivity.this, "Value cannot be greater than 5",
                    Toast.LENGTH_SHORT).show();}

        ListView listView = (ListView) parentRow.getParent();
        final int position = listView.getPositionForView(parentRow);
        symptomSeverity.set(position, symptomNumber);
        symptomDetails.get(position).setSeverity(symptomNumber);
        severityView.setText(String.valueOf(symptomNumber));
    }

    public void onUploadClick(View view) {

        HashMap<String, String> symptomToSeverity = new HashMap<>();
        int index = 0;
        for(String symptomType: Constants.symptomsDBColumns){
            symptomToSeverity.put(symptomType, symptomSeverity.get(index++).toString());
        }

        SaveSymptoms saveSymptoms = new SaveSymptoms();
        symptomToSeverity.put("heart_rate", heartRate.toString());
        symptomToSeverity.put("respiratory_rate", respiratoryRate.toString());
        saveSymptoms.execute(symptomToSeverity);
        Toast.makeText(SymptomsActivity.this, "Symptoms and Rates persisted!",
                Toast.LENGTH_SHORT).show();
        finish();
    }
}