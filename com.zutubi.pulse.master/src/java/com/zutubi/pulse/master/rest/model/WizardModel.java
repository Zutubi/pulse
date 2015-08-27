package com.zutubi.pulse.master.rest.model;

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

    public void addStep(WizardStepModel step)
    {
        steps.add(step);
    }
}
