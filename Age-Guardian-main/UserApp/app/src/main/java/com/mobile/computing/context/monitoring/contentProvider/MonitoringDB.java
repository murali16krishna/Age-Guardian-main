package com.mobile.computing.context.monitoring.contentProvider;

import static com.mobile.computing.context.monitoring.utils.Constants.COUGH;
import static com.mobile.computing.context.monitoring.utils.Constants.DIARRHEA;
import static com.mobile.computing.context.monitoring.utils.Constants.FEELING_TIRED;
import static com.mobile.computing.context.monitoring.utils.Constants.FEVER;
import static com.mobile.computing.context.monitoring.utils.Constants.HEADACHE;
import static com.mobile.computing.context.monitoring.utils.Constants.HEART_RATE;
import static com.mobile.computing.context.monitoring.utils.Constants.LOSS_OF_SMELL_OR_TASTE;
import static com.mobile.computing.context.monitoring.utils.Constants.MUSCLE_ACHE;
import static com.mobile.computing.context.monitoring.utils.Constants.NAUSEA;
import static com.mobile.computing.context.monitoring.utils.Constants.RESPIRATORY_RATE;
import static com.mobile.computing.context.monitoring.utils.Constants.SHORTNESS_OF_BREATH;
import static com.mobile.computing.context.monitoring.utils.Constants.SOAR_THROAT;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

public class MonitoringDB extends SQLiteOpenHelper {

    public static final String DB_NAME = "context_monitoring";

    public static final String TABLE = "symptoms_data";

    public static final String ID = "id";

    private static final String CREATE_TABLE_SYMPTOMS_DATA_QUERY = "CREATE TABLE " + TABLE + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + HEART_RATE + " INTEGER,"
            + RESPIRATORY_RATE + " INTEGER,"
            + NAUSEA + " INTEGER,"
            + HEADACHE + " INTEGER,"
            + DIARRHEA + " INTEGER,"
            + SOAR_THROAT + " INTEGER,"
            + FEVER + " INTEGER,"
            + MUSCLE_ACHE + " INTEGER,"
            + LOSS_OF_SMELL_OR_TASTE + " INTEGER,"
            + COUGH + " INTEGER,"
            + SHORTNESS_OF_BREATH + " INTEGER,"
            + FEELING_TIRED + " INTEGER)";

    private static final String DROP_TABLE_IF_EXISTS_QUERY = "DROP TABLE IF EXISTS " + TABLE;

    public MonitoringDB(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_SYMPTOMS_DATA_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_IF_EXISTS_QUERY);
        onCreate(db);
    }

    public void insertSymptomDetail(int heartRate, int respiratoryRate, HashMap<String, Integer> symptomSeverityMap) {

        SQLiteDatabase dbClient = null;
        try {
            dbClient = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(HEART_RATE, heartRate);
            values.put(RESPIRATORY_RATE, respiratoryRate);

            for (Map.Entry<String, Integer> entry : symptomSeverityMap.entrySet())
                values.put(entry.getKey(), entry.getValue());

            dbClient.insert(TABLE, null, values);

        } catch (Exception e) {
            System.out.println("Unexpected error occurred while inserting to table " + e.getMessage());
            e.printStackTrace();

        } finally {
            if(dbClient != null) dbClient.close();
        }
    }
}
