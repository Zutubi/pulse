package com.zutubi.prototype.model;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class Wizard
{
    private List<String> steps = new LinkedList<String>();
    private int currentStep;

    private boolean decorate;
    private Form form;

    public int getStepCount()
    {
        return steps.size();
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

    public List<String> getSteps()
    {
        return steps;
    }

    public void addStep(String name)
    {
        steps.add(name);
    }
}
