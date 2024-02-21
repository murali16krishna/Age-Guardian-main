package com.mobile.computing.context.monitoring;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HeartRateService {

    public interface HeartRateCallback {
        void onHeartRateCalculated(String heartRate);
    }

    public void calculateHeartRate(final String videoPath, final HeartRateCallback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                List<Bitmap> frameList = new ArrayList<>();

                try {
                    Log.i("log", "Required " + videoPath);
                    retriever.setDataSource(videoPath);
                    String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT);
                    int duration = Integer.parseInt(durationStr) / 12;
                    System.gc();
                    Log.i("log", "duration " + duration);

                    int i = 10;
                    while (i < duration) {
                        Log.i("log", "iteration " + i);
                        Bitmap bitmap = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            bitmap = retriever.getFrameAtIndex(i);
                        }
                        frameList.add(bitmap);
                        i += 5;
                    }
                    Log.i("log", "framelist " + frameList.size());

                } catch (Exception e) {
                    Log.i("log", "exception " + e);
                    // Handle or log exception
                } finally {
                    try {
                        retriever.release();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                long redBucket = 0;
                long pixelCount = 0;
                List<Long> redBucketList = new ArrayList<>();
                Log.i("log", "framelist " + frameList.size());
                for (Bitmap frame : frameList) {
                    Log.i("log", "framesize " + frame.getHeight() + " " + frame.getWidth());
                    redBucket = 0;
                    for (int y = 0; y < frame.getHeight(); y++) {
                        for (int x = 0; x < frame.getWidth(); x++) {
                            int c = frame.getPixel(x, y);
                            pixelCount++;
                            redBucket += Color.red(c) + Color.blue(c) + Color.green(c);
                        }
                    }
                    redBucketList.add(redBucket);
                }

                List<Long> avgBucketList = new ArrayList<>();
                for (int i = 0; i < redBucketList.size() - 5; i++) {
                    long temp = (redBucketList.get(i) + redBucketList.get(i + 1) +
                            redBucketList.get(i + 2) + redBucketList.get(i + 3) + redBucketList.get(i + 4)) / 4;
                    avgBucketList.add(temp);
                }

                long previousBucketValue = avgBucketList.get(0);
                int count = 0;
                for (int i = 1; i < avgBucketList.size() - 1; i++) {
                    long currentBucketValue = avgBucketList.get(i);
                    if ((currentBucketValue - previousBucketValue) > 3500) {
                        count++;
                    }
                    previousBucketValue = currentBucketValue;
                }

                int rate = (int) ((count * 12 / 45.0) * 60);
                Log.i("log", "rate " + rate);

                callback.onHeartRateCalculated(String.valueOf(rate / 2));
            }
        }).start();
    }
}
