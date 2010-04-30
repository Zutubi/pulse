package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.type.CompositeType;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class Wizard
{
    private List<WizardStep> steps = new LinkedList<WizardStep>();
    private String currentStep;

    private boolean decorate;
    private boolean templateCollectionItem;
    private Form form;

    public int getStepCount()
    {
        return steps.size();
    }

    public String getCurrentStep()
    {
        return currentStep;
    }

    public void setCurrentStep(String currentStep)
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

    public boolean isTemplateCollectionItem()
    {
        return templateCollectionItem;
    }

    public void setTemplateCollectionItem(boolean templateCollectionItem)
    {
        this.templateCollectionItem = templateCollectionItem;
    }

    public Form getForm()
    {
        return form;
    }

    public void setForm(Form form)
    {
        this.form = form;
    }

    public List<WizardStep> getSteps()
    {
        return steps;
    }

    public void addStep(String id, CompositeType type, String name)
    {
        steps.add(new WizardStep(id, type, name));
    }
}
