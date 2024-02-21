package com.mobile.computing.context.guardianapp;

public class QueueMessageBody {

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
