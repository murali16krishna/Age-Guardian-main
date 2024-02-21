package com.mobile.computing.context.monitoring.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mobile.computing.context.monitoring.R;
import com.mobile.computing.context.monitoring.entities.SymptomDetail;

import java.util.List;

public class SymptomDetailAdapter extends ArrayAdapter<SymptomDetail> {

    private static final String LOG_TAG = SymptomDetailAdapter.class.getSimpleName();

    public SymptomDetailAdapter(Activity context, List<SymptomDetail> symptoms) {
        super(context, 0, symptoms);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.symptoms_list, parent, false);
        }

        SymptomDetail currentDetail = getItem(position);

        TextView nameTextView = (TextView) listItemView.findViewById(R.id.symptom_type);
        nameTextView.setText(currentDetail.getSymptom());

        TextView numberTextView = (TextView) listItemView.findViewById(R.id.symptom_severity);
        numberTextView.setText(String.valueOf(currentDetail.getSeverity()));

        return listItemView;
    }
}
