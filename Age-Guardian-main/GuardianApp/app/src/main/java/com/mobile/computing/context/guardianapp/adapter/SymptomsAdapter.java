package com.mobile.computing.context.guardianapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.computing.context.guardianapp.QueueMessageBody;
import com.mobile.computing.context.guardianapp.R;

import java.util.Map;

public class SymptomsAdapter extends RecyclerView.Adapter<SymptomsAdapter.SymptomViewHolder> {
    private Map<String, Integer> symptoms;

    public SymptomsAdapter(Map<String, Integer> symptoms) {
        this.symptoms = symptoms;
    }

    @NonNull
    @Override
    public SymptomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.symptom_item, parent, false);
        return new SymptomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomViewHolder holder, int position) {
        String symptom = (String) symptoms.keySet().toArray()[position];
        Integer severity = symptoms.get(symptom);
        holder.bind(symptom, severity);
    }

    @Override
    public int getItemCount() {
        return symptoms.size();
    }

    public static class SymptomViewHolder extends RecyclerView.ViewHolder {
        private TextView symptomName;
        private View symptomSeverityMarker;

        public SymptomViewHolder(@NonNull View itemView) {
            super(itemView);
            symptomName = itemView.findViewById(R.id.symptomName);
            symptomSeverityMarker = itemView.findViewById(R.id.symptomSeverityMarker);
        }

        public void bind(String symptom, int severity) {
            symptomName.setText(symptom);

            itemView.post(() -> {
                int totalWidth = itemView.getWidth();
                int markerPosition = (totalWidth * severity) / 10;
                LinearLayout.LayoutParams layoutParams =
                        (LinearLayout.LayoutParams) symptomSeverityMarker.getLayoutParams();
                layoutParams.setMarginStart(markerPosition);
                symptomSeverityMarker.setLayoutParams(layoutParams);
            });
        }
    }
}

