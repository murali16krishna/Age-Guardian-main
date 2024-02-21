package com.mobile.computing.context.monitoring.service;

import android.content.Context;

import com.mobile.computing.context.monitoring.R;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CsvReaderService {

    Context context;
    List<String[]> rows = new ArrayList<>();

    public CsvReaderService(Context context) {
        this.context = context;
    }

    public List<List<Float>> readCSV() {
        List<List<Float>> res = new LinkedList<>();
        try {
            InputStream is = context.getResources().openRawResource(R.raw.csvbreathe27v1);;
            InputStreamReader isr = new InputStreamReader(is);


            CSVReader csvReader = new CSVReaderBuilder(isr)
                    .build();
            List<String[]> allData = csvReader.readAll();
            List<Float> cellValues = new LinkedList<>();
            for (String[] row : allData) {
                try{
                    cellValues.add(Float.valueOf(row[0]));
                }
                catch (NumberFormatException e) {

                }
            }

            List<Float> x = cellValues.subList(0,1280);
            List<Float> y = cellValues.subList(1280,2560);
            List<Float> z = cellValues.subList(2560,cellValues.size());

            res.add(x);
            res.add(y);
            res.add(z);
        }
        catch (IOException e){
            System.out.println(e.fillInStackTrace());
        }

        return res;
    }
}
