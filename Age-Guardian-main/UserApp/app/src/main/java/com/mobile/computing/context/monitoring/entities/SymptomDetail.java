package com.mobile.computing.context.monitoring.entities;

public class SymptomDetail {

    private String symptom;

    private Integer severity;

    public SymptomDetail(String type, int severity) {
        symptom = type;
        this.severity = severity;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getSymptom() {
        return symptom;
    }

    public Integer getSeverity() {
        return severity;
    }
}
