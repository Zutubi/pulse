package com.zutubi.prototype.model;

/**
 *
 *
 */
public class Wizard
{
    private int stepCount;
    private int currentStep;

    private boolean decorate;

    private Form form;

    public int getStepCount()
    {
        return stepCount;
    }

    public void setStepCount(int stepCount)
    {
        this.stepCount = stepCount;
    }

    public int getCurrentStep()
    {
        return currentStep;
    }

    public void setCurrentStep(int currentStep)
    {
        this.currentStep = currentStep;
    }

    public boolean isDecorate()
    {
        return decorate;
    }

    public void setDecorate(boolean decorate)
    {
        this.decorate = decorate;
    }

    public Form getForm()
    {
        return form;
    }

    public void setForm(Form form)
    {
        this.form = form;
    }
}
