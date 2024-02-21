package com.mobile.computing.context.monitoring.utils;

import java.util.List;

public class Constants {

    public static final String HEART_RATE = "heartRate";
    public static final String RESPIRATORY_RATE = "respiratoryRate";
    public static final String NAUSEA = "nausea";
    public static final String HEADACHE = "headache";
    public static final String DIARRHEA = "diarrhea";
    public static final String SOAR_THROAT = "soarThroat";
    public static final String FEVER = "fever";
    public static final String MUSCLE_ACHE = "muscleAche";
    public static final String LOSS_OF_SMELL_OR_TASTE = "lossOfSmellOrTaste";
    public static final String COUGH = "cough";
    public static final String SHORTNESS_OF_BREATH = "shortnessOfBreath";
    public static final String FEELING_TIRED = "feelingTired";

    public static final String TEMPERATURE = "temperature";
    public static final String WIND_SPEED = "windSpeed";
    public static final String UV_INDEX = "uvIndex";
    public static final String DANGER_LEVEL = "dangerLevel";

    public static final String LAT = "latitude";
    public static final String LONG = "longitude";
    public static final String RISK_METER = "risk-Level";



    public static List<String> symptomsDBColumns = List.of(NAUSEA, HEADACHE, DIARRHEA,
            SOAR_THROAT, FEVER, MUSCLE_ACHE,
            LOSS_OF_SMELL_OR_TASTE, COUGH,
            SHORTNESS_OF_BREATH, FEELING_TIRED);
}
