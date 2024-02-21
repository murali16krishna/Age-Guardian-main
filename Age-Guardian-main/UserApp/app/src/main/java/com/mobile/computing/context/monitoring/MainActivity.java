package com.mobile.computing.context.monitoring;

import static com.mobile.computing.context.monitoring.utils.Constants.DANGER_LEVEL;
import static com.mobile.computing.context.monitoring.utils.Constants.HEART_RATE;
import static com.mobile.computing.context.monitoring.utils.Constants.RESPIRATORY_RATE;
import static com.mobile.computing.context.monitoring.utils.Constants.TEMPERATURE;
import static com.mobile.computing.context.monitoring.utils.Constants.UV_INDEX;
import static com.mobile.computing.context.monitoring.utils.Constants.WIND_SPEED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.mobile.computing.context.monitoring.service.CsvReaderService;
import com.mobile.computing.context.monitoring.service.EmailSenderService;
import com.mobile.computing.context.monitoring.service.SQSPollingService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            new DynamoDBTask().execute();
        }
    };

    ExecutorService service;
    private SensorManager acclmeterManager;
    private Sensor acclmeterSensor;
    private SensorManager gyroSensorManager;
    private Sensor gyrometerSensor;

    private final float[] acclValX = new float[451];
    private final float[] acclValY = new float[451];
    private final float[] acclValZ = new float[451];

    Recording recording = null;
    Uri uri = null;
    private int index = 0;
    VideoCapture<Recorder> videoRecord = null;
    Integer heartRate = 0;
    Integer respiratoryRate = 0;
    static Boolean videoUploadedStatus = false;

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setUvIndex(double uvIndex) {
        this.uvIndex = uvIndex;
    }


    PreviewView videoView;
    HeartRateCallBackService heartRateCallBack = null;
    private double latitude = 0.0;

    Double temperature = 0.0;
    double windSpeed =  0.0;
    double uvIndex =  0.0;
    double dangerLevel = 0.0;

    public Double getTemperature() {
        return temperature;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getUvIndex() {
        return uvIndex;
    }

    public double getDangerLevel() {
        return dangerLevel;
    }

    public void setDangerLevel(double dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    private double longitude = 0.0;

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    EmailSenderService emailSenderService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button heartRate = findViewById(R.id.heartRateButton);
        Button respiratoryRate = findViewById(R.id.respiratoryRateButton);
        acclmeterManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acclmeterSensor = acclmeterManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        int PERMISSION_ALL = 1;
        String[] REQUIRED_PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
        };

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_ALL);
        }

        videoView = (PreviewView) findViewById(R.id.previewVideo);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        heartRate.setOnClickListener(view -> {
            Recorder recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build();
            videoRecord = VideoCapture.withOutput(recorder);

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                recordVideo();
        });

        respiratoryRate.setOnClickListener(view -> {
            Toast.makeText(this, "Respiratory rate calculation started...", Toast.LENGTH_SHORT).show();
            acclmeterManager.registerListener((SensorEventListener) MainActivity.this, acclmeterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        });

        gyroSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyrometerSensor = gyroSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroSensorManager.registerListener((SensorEventListener) MainActivity.this, gyrometerSensor, 999999999);
        checkPermissions();
        service = Executors.newSingleThreadExecutor();

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private void recordVideo() {
        if (stopVideoRecordingIfRecordIsInProgress()) return;

        ContentValues contentValues = setVideoMetaData();

        MediaStoreOutputOptions options;
        options = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();

        Camera camera;
        camera = setCameraSettings();

        enableCameraFlash(camera);

        recording = videoRecord.getOutput().prepareRecording(MainActivity.this, options).start(ContextCompat.getMainExecutor(MainActivity.this), videoRecordEvent -> {
            if (videoRecordEvent instanceof VideoRecordEvent.Start) {
            } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                    uri = ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                    String msg = "Video capture succeeded: " + uri;
                    camera.getCameraControl().enableTorch(false);
                    recording = null;
                    videoRecord = null;
                    SlowTask slowTask = new SlowTask();
                    slowTask.execute();
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                } else {
                    recording.close();
                    recording = null;
                    String msg = "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    @SuppressLint("StaticFieldLeak")
    public class SlowTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TextView heartdata = findViewById(R.id.heartRateTextView);
            heartdata.setText(String.valueOf(s));
            heartRate = Integer.valueOf(s);
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected String doInBackground(String... params) {
            List<Bitmap> frameList = new ArrayList<>();
            MediaMetadataRetriever mediaRetriever = new MediaMetadataRetriever();
            int videoDuration=0;
            try {
                mediaRetriever.setDataSource(getBaseContext(),uri);
                String duration = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                assert duration != null;
                videoDuration = Integer.parseInt(duration);
                int i = 10;
                while (i < videoDuration/75) {
                    Bitmap bitmap = mediaRetriever.getFrameAtIndex(i);
                    frameList.add(bitmap);
                    i += 5;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    mediaRetriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                long redBucket;
                List<Long> a = new ArrayList<>();
                for (Bitmap frame : frameList) {
                    redBucket = 0;
                    for (int y = 550; y < 650; y++) {
                        for (int x = 550; x < 650; x++) {
                            int color = frame.getPixel(x, y);
                            redBucket += Color.red(color) + Color.blue(color) + Color.green(color);
                        }
                    }
                    a.add(redBucket);
                }
                List<Long> b = new ArrayList<>();
                for (int i = 0; i < a.size() - 5; i++) {
                    long temp = (a.get(i) + a.get(i + 1) + a.get(i + 2) + a.get(i + 3) + a.get(i + 4)) / 4;
                    b.add(temp);
                }
                long x = b.get(0);
                int count = 0;
                for (int i = 1; i < b.size(); i++) {
                    long p = b.get(i);
                    if ((p - x) > 1000) {
                        count++;
                    }
                    x = b.get(i);
                }
                int rate = (int) ((count * 60.0) / (videoDuration / 1000.0) / 2);
                return String.valueOf(rate);
            }
        }

    }

    private boolean stopVideoRecordingIfRecordIsInProgress() {
        Recording recording1 = recording;
        if (recording1 != null) {
            recording1.stop();
            recording=null;
            return true;
        }
        return false;
    }

    private static void enableCameraFlash(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                camera.getCameraControl().enableTorch(true);
            }
        }
    }

    @NonNull
    private Camera setCameraSettings() {
        Camera camera;
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(MainActivity.this);
        ProcessCameraProvider cameraProvider;
        try {
            cameraProvider = processCameraProvider.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(videoView.getSurfaceProvider());
        cameraProvider.unbindAll();
        CameraSelector cameraSelector;
        cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoRecord);
        return camera;
    }

    @NonNull
    private static ContentValues setVideoMetaData() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis()));
        return contentValues;
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getWeatherInfo(double latitude, double longitude) {
        String apiKey = "973b497e2d014d718ee80919233011";
        String urlString = "https://api.weatherapi.com/v1/current.json?key=" + apiKey + "&q=" + latitude + "," + longitude;


        new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        parseWeatherInfo(stringBuilder.toString());
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseWeatherInfo(String jsonResponse) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONObject currentWeather = jsonObject.getJSONObject("current");

                    double temperature = currentWeather.getDouble("temp_c");
                    double windSpeed = currentWeather.getDouble("wind_kph");
                    double uvIndex = currentWeather.getDouble("uv");

                    setTemperature(temperature);
                    setWindSpeed(windSpeed);
                    setUvIndex(uvIndex);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                setLatitude(latitude);
                                setLongitude(longitude);
                                getWeatherInfo(latitude, longitude);
                            }
                        });
            } catch (SecurityException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView heartRateTextField = (TextView) findViewById(R.id.heartRateTextView);
        heartRateTextField.setText("00");
        TextView respiratoryRateTextField = (TextView) findViewById(R.id.respiratoryRateTextView);
        respiratoryRateTextField.setText("00");
        videoView.setVisibility(View.VISIBLE);
        //heartRateCallBack.heartRate = null;
        videoUploadedStatus = false;
    }

    public void launchSymptomsActivity(View view) {
        if (heartRate == null || respiratoryRate == 0) {
            Toast.makeText(MainActivity.this, "Calculate Heart/Respiratory Rates Prior to Assessing Symptoms",
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, SymptomsActivity.class);
            intent.putExtra(HEART_RATE, heartRate);
            intent.putExtra(RESPIRATORY_RATE, respiratoryRate);
            startActivity(intent);
        }
    }

    public void launchWeatherActivity(View view) {
        if (heartRate == null || respiratoryRate == 0) {
            Toast.makeText(MainActivity.this, "Calculate Heart/Respiratory Rates Prior to Assessing Symptoms",
                    Toast.LENGTH_SHORT).show();
        } else {
            getRiskMeter();
            Intent intent = new Intent(this, WeatherActivity.class);
            intent.putExtra(HEART_RATE, heartRate);
            intent.putExtra(RESPIRATORY_RATE, respiratoryRate);
            intent.putExtra(TEMPERATURE, temperature);
            intent.putExtra(WIND_SPEED, windSpeed);
            intent.putExtra(UV_INDEX, uvIndex);
            intent.putExtra(DANGER_LEVEL,dangerLevel);
            startActivity(intent);
        }
    }

    public void getMyGuardian(View view)
    {

        Intent intent = new Intent(this, ViewGuardianActivity.class);

        startActivity(intent);
    }

    public void onClickUploadSignsButton(View view) {

        if(heartRate == null && respiratoryRate == 0) {
            makeToast("Calculate Heart/Respiratory Rates!");
        } else if(heartRate == null && respiratoryRate != 0){
            makeToast("Calculate Heart Rate!");
        } else if (heartRate != null && respiratoryRate == 0) {
            makeToast("Calculate Respiratory Rate!");
        } else{
            Toast.makeText(MainActivity.this, "Rates calculated, " +
                            "Click Symptoms to update Severities",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void getRiskMeter() {
        if (heartRate==null) {
            Toast.makeText(MainActivity.this, "Need Heart Rate first!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (respiratoryRate==null) {
            Toast.makeText(MainActivity.this, "Need Resp Rate also :)",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if(temperature==null) {
            Toast.makeText(MainActivity.this, "Finally need Weather Info also :p",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        FuzzyRiskMeter fuzzyRiskMeter = new FuzzyRiskMeter();
        double dangerLevel = fuzzyRiskMeter.evaluateRiskMeter(heartRate, 22, 22.3);
        setDangerLevel(dangerLevel);
    }


    private void makeToast(String text) {
        Toast.makeText(MainActivity.this, text,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        int acclmeterdata;
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            index++;
            acclValX[index] = sensorEvent.values[0];
            acclValY[index] = sensorEvent.values[1];
            acclValZ[index] = sensorEvent.values[2];
            if(index >= 450){
                index = 0;
                acclmeterManager.unregisterListener(MainActivity.this);
                acclmeterdata = callRespiratoryCalculator();
                TextView respiratoryrateData = findViewById(R.id.respiratoryRateTextView);
                respiratoryrateData.setText(String.valueOf(acclmeterdata));
                respiratoryRate = acclmeterdata;
            }

        }
        
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float gyroX = sensorEvent.values[0];
            float gyroY = sensorEvent.values[1];
            float gyroZ = sensorEvent.values[2];

            // Calculate the overall force
            float totalForce = (float) Math.sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ);
            System.out.println("totalForce = " + totalForce);

            if (totalForce > 2.0f) {
                // Fall detected
                System.out.println("fallHappened = " + totalForce);
                emailSenderService = new EmailSenderService();
                emailSenderService.setSenderEmail("praveenu193@gmail.com");
                emailSenderService.setRecipientEmail("tejesh.silarapu2020@gmail.com");
                emailSenderService.setSubject("Fall Detected!!");
                emailSenderService.setCustomMessage("The User 1 Has Fell Down. Please take appropriate action");

                emailSenderService.execute();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public int callRespiratoryCalculator(){
        float prevValue;
        float currValue;
        prevValue = 10f;
        int k=0;
        for (int i = 11; i <= 450; i++) {
            currValue = (float) Math.sqrt(
                    Math.pow(acclValZ[i], 2.0) + Math.pow(acclValX[i], 2.0) + Math.pow(acclValY[i], 2.0)
            );
            if (Math.abs(prevValue - currValue) > 0.15) {
                k++;
            }
            prevValue = currValue;
        }

        double ret = (double) k / 45.00;
        return (int) (ret * 30);
    }

    private class DynamoDBTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            final String awsAccessKey = "AKIAULVF54ZQ54WJY6FJ";
            final String awsSecretKey = "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";
            AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
            AmazonSQS sqs = new AmazonSQSClient(credentials);
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentials);
            ddbClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            Gson gson = new Gson();
            while (!isCancelled()) {
                getLocation();
                getWeatherInfo(latitude, longitude);
                getRiskMeter();

                String responseQueueUrl = "https://sqs.us-east-2.amazonaws.com/299921958497/userDetailsResponseQueue";

                HashMap<String, AttributeValue> key = new HashMap<>();
                key.put("id", new AttributeValue().withS("1"));

                GetItemRequest request = new GetItemRequest()
                        .withTableName("user")
                        .withKey(key);
                String firstName = null;
                GetItemResult result = ddbClient.getItem(request);
                if (result != null && result.getItem() != null) {
                    Map<String, AttributeValue> item = result.getItem();
                    firstName = item.get("first_name").getS();
                }
                SQSPollingService.Weather weather = new SQSPollingService.Weather(getTemperature(), getWindSpeed(), 0D, 0D, getUvIndex());
                SQSPollingService.QueueMessageBody queueMessageBody =
                        new SQSPollingService.QueueMessageBody("1", "firstName", weather, getLatitude(), getLongitude(), (int) getDangerLevel());
                String body = gson.toJson(queueMessageBody);
                SendMessageRequest sendMsgRequest = new SendMessageRequest()
                        .withQueueUrl(responseQueueUrl)
                        .withMessageBody(body);

                sqs.sendMessage(sendMsgRequest);
                try {
                    Thread.sleep(3000);  // Sleep for 15 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;  // Exit the loop if the thread is interrupted
                }
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}