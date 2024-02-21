package com.mobile.computing.context.monitoring;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Calendar;

public class NavigationActivity extends AppCompatActivity {

    String src = "";
    String dest = "";

    TextView resultTextView;
    TextView cognitiveTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        resultTextView = (TextView) findViewById(R.id.resultView);

        cognitiveTextView = (TextView) findViewById(R.id.cognitiveView);
    }

    public void onGetTrafficClick(View view) {

//        String sourceLocation = "33.40969,-111.91075";
//        String destinationLocation = "35.19745,-111.65019";


        EditText srcField = (EditText) findViewById(R.id.sourceEditTextView);
        src = srcField.getText().toString();
        EditText destField = (EditText) findViewById(R.id.destinationEditTextView);
        dest = destField.getText().toString();

        String startAddress = src;
        String endAddress = dest;
        // String apiKey = getResources().getString(R.string.google_maps_key);

        String csvHeader = "Start Address,End Address,Duration,Duration In Traffic,Distance,Average Speed,Current Speed\n";
        //String csvFileName = "traffic_data.csv";
        File dir = getExternalFilesDir(null);
        //File file = new File(dir, csvFileName);
        FileWriter fileWriter = null;

        //boolean isNewFile = !file.exists();

        if (!startAddress.isEmpty() && !endAddress.isEmpty()) {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey("AIzaSyDSRNvk3XoKiXdHLV6lk0dDF5lHCEdLAoo")
                    .build();

            try {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, 10);
                Instant departureTime = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    departureTime = Instant.ofEpochMilli(calendar.getTimeInMillis());
                }

                DirectionsResult directionsResponse = DirectionsApi.newRequest(context)
                        .origin(startAddress)
                        .destination(endAddress)
                        .mode(TravelMode.DRIVING)
                        .departureTime(departureTime)
                        .await();

                //fileWriter = new FileWriter(file, true);

                // Write the header if the file didn't already exist
//                if (isNewFile) {
//                    fileWriter.append(csvHeader);
//                }

                for (DirectionsRoute route : directionsResponse.routes) {
                    for (DirectionsLeg leg : route.legs) {
                        long duration = leg.duration.inSeconds;
                        long durationInTraffic = (leg.durationInTraffic != null) ? leg.durationInTraffic.inSeconds : duration;
                        long distance = leg.distance.inMeters;
                        float avgSpeed = (float) distance / duration;
                        float currSpeed = (float) distance / durationInTraffic;

                        // Append data to CSV
//                        fileWriter.append("\"" + startAddress + "\",\"" + endAddress + "\","
//                                + duration + "," + durationInTraffic + "," + distance + ","
//                                + avgSpeed + "," + currSpeed + "\n");

                        if (durationInTraffic > duration) {
                            resultTextView.setText("Condition: POOR\nCognitive Workload: HCW");
                        } else {
                            resultTextView.setText("Condition: NORMAL\nCognitive Workload: LCW");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileWriter != null) {
                        fileWriter.flush();
                        fileWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(NavigationActivity.this, "Provide Start and End Address.", Toast.LENGTH_SHORT).show();
        }
    }
}