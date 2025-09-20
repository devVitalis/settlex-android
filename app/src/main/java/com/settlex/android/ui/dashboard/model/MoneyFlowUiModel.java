package com.settlex.android.ui.dashboard.model;

public class MoneyFlowUiModel {

   private final double inFlow;
   private final double outFlow;

    public MoneyFlowUiModel(double inFlow, double outFlow) {
        this.inFlow = inFlow;
        this.outFlow = outFlow;
    }

    public double getInFlow() {
        return inFlow;
    }

    public double getOutFlow() {
        return outFlow;
    }
}
