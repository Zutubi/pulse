package com.zutubi.pulse.master.rest.model;

import java.util.Set;

/**
 * Information about a type in a wizard. Wraps a {@link CompositeTypeModel} with extra
 * wizard-specific details.
 */
public class WizardTypeModel
{
    private CompositeTypeModel type;
    private String label;
    private String help;
    private WizardTypeFilter filter;

    public WizardTypeModel(CompositeTypeModel type, String label)
    {
        this.type = type;
        this.label = label;
    }

    public CompositeTypeModel getType()
    {
        return type;
    }

    public String getLabel()
    {
        return label;
    }

    public String getHelp()
    {
        return help;
    }

    public void setHelp(String help)
    {
        this.help = help;
    }

    public WizardTypeFilter getFilter()
    {
        return filter;
    }

    public void setTypeFilter(String stepKey, Set<String> compatibleTypes)
    {
        filter = new WizardTypeFilter(stepKey, compatibleTypes);
    }

    public static class WizardTypeFilter
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
}
