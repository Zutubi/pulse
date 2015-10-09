package com.zutubi.pulse.master.rest.model;

/**
 * Abstract base for steps of a wizard.
 */
public abstract class WizardStepModel
{
    protected String key;
    protected String label;

    public WizardStepModel(String label, String key)
    {
        this.label = label;
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public String getLabel()
    {
        return label;
    }
}
