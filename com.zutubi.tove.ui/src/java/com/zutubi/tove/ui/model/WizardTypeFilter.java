package com.zutubi.tove.ui.model;

import java.util.Set;

/**
 * Filters availability of a wizard step type based on the selected type in an earlier step.
 */
public class WizardTypeFilter
{
    private String stepKey;
    private Set<String> compatibleTypes;

    public WizardTypeFilter(String stepKey, Set<String> compatibleTypes)
    {
        this.stepKey = stepKey;
        this.compatibleTypes = compatibleTypes;
    }

    public String getStepKey()
    {
        return stepKey;
    }

    public Set<String> getCompatibleTypes()
    {
        return compatibleTypes;
    }
}
