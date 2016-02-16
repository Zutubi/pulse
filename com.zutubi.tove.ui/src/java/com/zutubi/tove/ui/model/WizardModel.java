package com.zutubi.tove.ui.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Wizards are used to help guide creation of composites for insertion.
 */
public class WizardModel
{
    private List<WizardStepModel> steps = new ArrayList<>();

    public List<WizardStepModel> getSteps()
    {
        return steps;
    }

    public void prependStep(WizardStepModel step)
    {
        steps.add(0, step);
    }

    public void appendStep(WizardStepModel step)
    {
        steps.add(step);
    }
}
