package com.mobile.computing.context.monitoring;

import com.fuzzylite.Engine;
import com.fuzzylite.defuzzifier.Centroid;
import com.fuzzylite.norm.s.Maximum;
import com.fuzzylite.norm.t.AlgebraicProduct;
import com.fuzzylite.rule.Rule;
import com.fuzzylite.rule.RuleBlock;
import com.fuzzylite.term.Ramp;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import com.fuzzylite.norm.t.Minimum;

public class FuzzyRiskMeter {
    private Engine engine;
    private InputVariable heartRate;
    private InputVariable respiratoryRate;
    private InputVariable weather;
    private OutputVariable riskMeter;

    public FuzzyRiskMeter() {
        engine = new Engine();
        engine.setName("RiskMeter");
        engine.setDescription("");

        heartRate = new InputVariable();
        heartRate.setName("heartRate");
        heartRate.setDescription("");
        heartRate.setEnabled(true);
        heartRate.setRange(50.000, 200.000);
        heartRate.addTerm(new Ramp("high", 100.000, 200.000));
        heartRate.addTerm(new Ramp("low", 200.000, 100.000));
        engine.addInputVariable(heartRate);

        respiratoryRate = new InputVariable();
        respiratoryRate.setName("respiratoryRate");
        respiratoryRate.setDescription("");
        respiratoryRate.setEnabled(true);
        respiratoryRate.setRange(10.000, 40.000);
        respiratoryRate.addTerm(new Ramp("high", 20.000, 40.000));
        respiratoryRate.addTerm(new Ramp("low", 40.000, 20.000));
        engine.addInputVariable(respiratoryRate);

        weather = new InputVariable();
        weather.setName("weather");
        weather.setDescription("");
        weather.setEnabled(true);
        weather.setRange(-10, 40);
        weather.addTerm(new Ramp("bad", 15.000, 0.000));
        weather.addTerm(new Ramp("good", 0.000, 15.000));
        engine.addInputVariable(weather);

        riskMeter = new OutputVariable();
        riskMeter.setName("riskMeter");
        riskMeter.setDescription("");
        riskMeter.setEnabled(true);
        riskMeter.setRange(0.000, 100.000);
        riskMeter.setAggregation(new Maximum());
        riskMeter.setDefuzzifier(new Centroid(100));
        riskMeter.setDefaultValue(Double.NaN);
        riskMeter.addTerm(new Ramp("low", 50.000, 0.000));
        riskMeter.addTerm(new Ramp("high", 0.000, 50.000));
        engine.addOutputVariable(riskMeter);

        RuleBlock ruleBlock = new RuleBlock();
        ruleBlock.setName("ruleBlock");
        ruleBlock.setDescription("");
        ruleBlock.setEnabled(true);
        ruleBlock.setConjunction(new Minimum());
        ruleBlock.setDisjunction(new Maximum());
        ruleBlock.setImplication(new AlgebraicProduct());
        ruleBlock.addRule(Rule.parse("if heartRate is high or respiratoryRate is high or weather is bad then riskMeter is high", engine));
        ruleBlock.addRule(Rule.parse("if heartRate is low and respiratoryRate is low and weather is good then riskMeter is low", engine));
        engine.addRuleBlock(ruleBlock);
    }

    public double evaluateRiskMeter(double hr, double rr, double weather) {
        heartRate.setValue(hr);
        respiratoryRate.setValue(rr);
        this.weather.setValue(weather);
        engine.process();
        return riskMeter.getValue();
    }
}
