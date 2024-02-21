package com.mobile.computing.context.monitoring;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

public class HeartRateCallBackService implements HeartRateService.HeartRateCallback {

    TextView textField;

    public String heartRate = null;

    public String getHeartRate() {
        return heartRate;
    }

    public HeartRateCallBackService(TextView textField) {
        this.textField = textField;
    }

    @Override
    public void onHeartRateCalculated(String heartRate) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            textField.setText(heartRate);
            textField.setTextColor(Color.WHITE);
        });
        this.heartRate = heartRate;
    }
}
