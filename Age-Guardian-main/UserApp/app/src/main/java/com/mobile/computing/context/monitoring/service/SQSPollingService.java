package com.mobile.computing.context.monitoring.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import com.mobile.computing.context.monitoring.MainActivity;

import java.util.List;

public class SQSPollingService extends Service {
    private static final String TAG = "SQSPollingService";
    private Handler handler;
    private boolean isRunning = false;

    Gson gson = new Gson();

    String requestQueueUrl = "https://sqs.us-east-2.amazonaws.com/299921958497/userDetailsRequestQueue";
    String responseQueueUrl = "https://sqs.us-east-2.amazonaws.com/299921958497/userDetailsResponseQueue";

    private final String awsAccessKey = "AKIAULVF54ZQ54WJY6FJ";
    private final String awsSecretKey = "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";

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            pollSQSQueue();
        }
        return START_STICKY;
    }

    private void pollSQSQueue() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

                AmazonSQS sqs = new AmazonSQSClient(credentials);

                while (isRunning) {
                    ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
                            .withQueueUrl(requestQueueUrl)
                            .withWaitTimeSeconds(5);

                    List<Message> messages = sqs.receiveMessage(receiveRequest).getMessages();

                    for (Message message : messages) {
                        String requestContent = message.getBody();
                        QueueMessageBody requestMessageBody = gson.fromJson(requestContent, QueueMessageBody.class);

                        try {
                            processRequest(requestMessageBody);
                        } catch (InterruptedException e) {
                            System.out.println("SOMETHING BROKE WHILE PROCESSING QUEUE REQUEST");
                            e.printStackTrace();
                        }

                        String body = gson.toJson(requestMessageBody);
                        SendMessageRequest sendMsgRequest = new SendMessageRequest()
                                .withQueueUrl(responseQueueUrl)
                                .withMessageBody(body);

                        sqs.sendMessage(sendMsgRequest);
                        sqs.deleteMessage(requestQueueUrl, message.getReceiptHandle());
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void processRequest(QueueMessageBody requestMessageBody) throws InterruptedException {
        MainActivity mainActivity = new MainActivity();
        mainActivity.getLocation();
        mainActivity.getRiskMeter();

        requestMessageBody.setLatitude(mainActivity.getLatitude());
        requestMessageBody.setLongitude(mainActivity.getLongitude());
        Weather weather = new Weather(mainActivity.getTemperature(), mainActivity.getWindSpeed(), 0D, 0D, mainActivity.getUvIndex());
        requestMessageBody.setWeather(weather);
        requestMessageBody.setRiskLevel((int) mainActivity.getDangerLevel());
        Thread.sleep(3000);
    }

    public static class QueueMessageBody {

        @Override
        public String toString() {
            return "QueueMessageBody{" +
                    "function:'" + function + '\'' +
                    ", userId:" + userId +
                    ", userName:'" + userName + '\'' +
                    ", weather:" + weather +
                    ", latitude:" + latitude +
                    ", longitude:" + longitude +
                    ", riskLevel:" + riskLevel +
                    '}';
        }

        public QueueMessageBody(String function, String userId, String userName, Weather weather, Double latitude, Double longitude, Integer riskLevel) {
            this.function = function;
            this.userId = userId;
            this.userName = userName;
            this.weather = weather;
            this.latitude = latitude;
            this.longitude = longitude;
            this.riskLevel = riskLevel;
        }

        public String getFunction() {
            return function;
        }

        public void setFunction(String function) {
            this.function = function;
        }

        private String function;

        private String userId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Weather getWeather() {
            return weather;
        }

        public void setWeather(Weather weather) {
            this.weather = weather;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Integer getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(Integer riskLevel) {
            this.riskLevel = riskLevel;
        }

        public QueueMessageBody(String userId, String userName, Weather weather, Double latitude, Double longitude, Integer riskLevel) {
            this.userId = userId;
            this.userName = userName;
            this.weather = weather;
            this.latitude = latitude;
            this.longitude = longitude;
            this.riskLevel = riskLevel;
        }

        private String userName;
        private Weather weather;

        public QueueMessageBody(String userId, String userName, Weather weather, Double latitude, Double longitude) {
            this.userId = userId;
            this.userName = userName;
            this.weather = weather;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public QueueMessageBody(String userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }

        private Double latitude;
        private Double longitude;
        private Integer riskLevel;

//        public class Weather {
//            private Double temp;
//            private Double wind;
//            private Double rainfall = 0.0;
//            private Double snowfall = 0.0;
//            private Double uvIndex;
//
//            public Double getTemp() {
//                return temp;
//            }
//
//            public void setTemp(Double temp) {
//                this.temp = temp;
//            }
//
//            public Double getWind() {
//                return wind;
//            }
//
//            public void setWind(Double wind) {
//                this.wind = wind;
//            }
//
//            public Double getRainfall() {
//                return rainfall;
//            }
//
//            public void setRainfall(Double rainfall) {
//                this.rainfall = rainfall;
//            }
//
//            public Double getSnowfall() {
//                return snowfall;
//            }
//
//            public void setSnowfall(Double snowfall) {
//                this.snowfall = snowfall;
//            }
//
//            public Double getUvIndex() {
//                return uvIndex;
//            }
//
//            public void setUvIndex(Double uvIndex) {
//                this.uvIndex = uvIndex;
//            }
//
//            public Weather(Double temp, Double wind, Double rainfall, Double snowfall, Double uvIndex) {
//                this.temp = temp;
//                this.wind = wind;
//                this.rainfall = rainfall;
//                this.snowfall = snowfall;
//                this.uvIndex = uvIndex;
//            }
//        }

    }

    public static class Weather {
        private Double temp;
        private Double wind;
        private Double rainfall = 0.0;
        private Double snowfall = 0.0;
        private Double uvIndex;

        public Double getTemp() {
            return temp;
        }

        public void setTemp(Double temp) {
            this.temp = temp;
        }

        public Double getWind() {
            return wind;
        }

        public void setWind(Double wind) {
            this.wind = wind;
        }

        public Double getRainfall() {
            return rainfall;
        }

        public void setRainfall(Double rainfall) {
            this.rainfall = rainfall;
        }

        public Double getSnowfall() {
            return snowfall;
        }

        public void setSnowfall(Double snowfall) {
            this.snowfall = snowfall;
        }

        public Double getUvIndex() {
            return uvIndex;
        }

        public void setUvIndex(Double uvIndex) {
            this.uvIndex = uvIndex;
        }

        public Weather(Double temp, Double wind, Double rainfall, Double snowfall, Double uvIndex) {
            this.temp = temp;
            this.wind = wind;
            this.rainfall = rainfall;
            this.snowfall = snowfall;
            this.uvIndex = uvIndex;
        }
    }
}
