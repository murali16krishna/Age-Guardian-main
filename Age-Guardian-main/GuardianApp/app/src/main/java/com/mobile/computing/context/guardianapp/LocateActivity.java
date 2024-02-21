package com.mobile.computing.context.guardianapp;

import static com.mobile.computing.context.guardianapp.utils.constants.AWSConfig.MAPS_API_KEY;
import static com.mobile.computing.context.guardianapp.utils.constants.Constants.USER_ID;
import static com.mobile.computing.context.guardianapp.utils.constants.Constants.USER_NAME;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.mobile.computing.context.guardianapp.service.SQSPoller;
import com.squareup.picasso.Picasso;

public class LocateActivity extends AppCompatActivity {

    String userId;
    String userName;
    SQSPoller sqsPoller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);

        sqsPoller = new SQSPoller();

        sqsPoller.startPolling(new SQSPoller.SQSListener() {
            @Override
            public void onMessageReceived(final String message) {

                Gson gson = new Gson();
                QueueMessageBody body = gson.fromJson(message, QueueMessageBody.class);
                runOnUiThread(() -> {
                    TextView temperature = findViewById(R.id.temperature);
                    temperature.setText(new StringBuilder().append(body.getWeather().getTemp()).append("°C").toString());

                    TextView windSpeed = findViewById(R.id.windSpeed);
                    windSpeed.setText(new StringBuilder().append(body.getWeather().getWind()).append(" km/h").toString());

                    TextView snowfall = findViewById(R.id.snowfall);
                    snowfall.setText(new StringBuilder().append(body.getWeather().getSnowfall()).append(" cm").toString());

                    TextView rainfall = findViewById(R.id.rainfall);
                    rainfall.setText(new StringBuilder().append(body.getWeather().getRainfall()).append(" mm").toString());

                    TextView uvIndex = findViewById(R.id.uvIndex);
                    uvIndex.setText(String.valueOf(body.getWeather().getUvIndex()));

                    Integer riskLevel = body.getRiskLevel();
                    setRiskLevel(riskLevel);
                    setRiskSeverity(riskLevel);

                    setLocationName(body.getLatitude(), body.getLongitude());
                    setMapsImage(body.getLatitude(), body.getLongitude());

                });
            }
        });

        userId = getIntent().getStringExtra(USER_ID);
        userName = getIntent().getStringExtra(USER_NAME);

         TextView userNameLocation = findViewById(R.id.userNameLocation);
         userNameLocation.setText(userName);

         setRiskLevel(70);

        TextView riskSeverityText = findViewById(R.id.riskSeverityText);

        String severityText = "SEVERE";
        riskSeverityText.setText(severityText);
    }

    private void setMapsImage(Double latitude, Double longitude) {
        ImageView staticMapImageView = findViewById(R.id.staticMapImageView);

        String staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
                "center=" + latitude + "," + longitude +
                "&zoom=15" +
                "&size=800x200" +
                "&markers=" + latitude + "," + longitude +
                "&key=" + MAPS_API_KEY;

        Picasso.get()
                .load(staticMapUrl)
                .into(staticMapImageView);
    }

    private void setLocationName(Double latitude, Double longitude) {
        try {
            GeoApiContext context = new GeoApiContext.Builder().apiKey(MAPS_API_KEY).build();
            LatLng latLng = new LatLng(latitude, longitude);
            GeocodingResult[] results = GeocodingApi.reverseGeocode(context, latLng).await();

            TextView locationName = findViewById(R.id.locationName);

            if (results != null && results.length > 0) {
                locationName.setText(results[0].formattedAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setRiskSeverity(Integer riskLevel) {
        TextView riskSeverityText = findViewById(R.id.riskSeverityText);
        String severityText;

        if (riskLevel >= 70) {
            severityText = "SEVERE";
        } else if (riskLevel >= 35 ) {
            severityText = "MODERATE";
        } else {
            severityText = "LOW";
        }
        riskSeverityText.setText(severityText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sqsPoller.stopPolling();
    }

    private void setRiskLevel(int riskLevel) {
        View riskMeterMarker = findViewById(R.id.riskMeterMarker);
        View riskMeter = findViewById(R.id.riskMeter);

        riskMeter.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int totalWidth = riskMeter.getWidth();
                int markerX = (int) ((riskLevel / 100.0) * totalWidth);

                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) riskMeterMarker.getLayoutParams();
                layoutParams.leftMargin = markerX;
                riskMeterMarker.setLayoutParams(layoutParams);

                riskMeter.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }
}
