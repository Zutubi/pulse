package com.zutubi.pulse.master.rest.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about a type in a wizard. Wraps a {@link CompositeTypeModel} with extra
 * wizard-specific details.
 */
public class WizardTypeModel
{
    private CompositeTypeModel type;
    private String label;
    private List<WizardTypeFilter> filters;

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

    public List<WizardTypeFilter> getFilters()
    {
        return filters;
    }

    public void addTypeFilter(String stepKey, List<String> compatibleTypes)
    {
        if (filters == null)
        {
            filters = new ArrayList<>();
        }

        filters.add(new WizardTypeFilter(stepKey, compatibleTypes));
    }

    public static class WizardTypeFilter
    {
        private String stepKey;
        private List<String> compatibleTypes = new ArrayList<>();

        public WizardTypeFilter(String stepKey, List<String> compatibleTypes)
        {
            this.stepKey = stepKey;
            this.compatibleTypes = compatibleTypes;
        }

        public String getStepKey()
        {
            return stepKey;
        }

        public List<String> getCompatibleTypes()
        {
            return compatibleTypes;
        }
    }
}
