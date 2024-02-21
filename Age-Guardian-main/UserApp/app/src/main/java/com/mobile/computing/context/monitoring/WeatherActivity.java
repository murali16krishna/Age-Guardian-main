package com.mobile.computing.context.monitoring;

import static com.mobile.computing.context.monitoring.utils.Constants.HEART_RATE;
import static com.mobile.computing.context.monitoring.utils.Constants.RESPIRATORY_RATE;
import static com.mobile.computing.context.monitoring.utils.Constants.TEMPERATURE;
import static com.mobile.computing.context.monitoring.utils.Constants.UV_INDEX;
import static com.mobile.computing.context.monitoring.utils.Constants.WIND_SPEED;
import static com.mobile.computing.context.monitoring.utils.Constants.DANGER_LEVEL;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

public class WeatherActivity extends AppCompatActivity {

    Integer heartRate = 0;
    Integer respiratoryRate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        Intent intent = getIntent();
        if (intent != null) {
            Double temperatureValue = intent.getDoubleExtra(TEMPERATURE, 0.0);
            Double windSpeedValue = intent.getDoubleExtra(WIND_SPEED, 0.0);
            Double uvIndexValue = intent.getDoubleExtra(UV_INDEX, 0.0);
            Double riskMeterValue = intent.getDoubleExtra(DANGER_LEVEL, 0.0);

            // Find the TextViews in your layout
            TextView windSpeedTextView = findViewById(R.id.windSpeed);
            TextView temperatureTextView = findViewById(R.id.temperature);
            TextView uvIndexTextView = findViewById(R.id.uvIndex);

            heartRate = intent.getIntExtra(HEART_RATE, 0);
            respiratoryRate = intent.getIntExtra(RESPIRATORY_RATE, 0);

            TextView heartRateValue = findViewById(R.id.heartRateValue);
            TextView respiratoryRateValue = findViewById(R.id.respiratoryRateValue);

            windSpeedTextView.setText(new StringBuilder().append(windSpeedValue).append(" km/hr").toString());
            temperatureTextView.setText(new StringBuilder().append(temperatureValue).append(" °C").toString());
            uvIndexTextView.setText(new StringBuilder().append(uvIndexValue).append("").toString());

            heartRateValue.setText(String.valueOf(heartRate));
            respiratoryRateValue.setText(String.valueOf(respiratoryRate));

            int riskMeterPosition = riskMeterValue.intValue();

            TextView riskSeverityText = findViewById(R.id.riskSeverityText);
            String severityText;

            if (riskMeterPosition >= 70) {
                severityText = "SEVERE";
            } else if (riskMeterPosition >= 40 ) {
                severityText = "MODERATE";
            } else {
                severityText = "LOW";
            }

            riskSeverityText.setText(severityText);
            setRiskLevel(riskMeterPosition);
        }
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