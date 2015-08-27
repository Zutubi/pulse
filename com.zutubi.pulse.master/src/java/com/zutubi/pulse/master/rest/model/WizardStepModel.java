package com.zutubi.pulse.master.rest.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A single step of a wizard.  If the type is known, a common case, there is only one entry in the
 * types list. Otherwise the user has a choice of types, effectively making this two steps.
 */
public class WizardStepModel
{
    private String key;
    private String label;
    private List<WizardTypeModel> types = new ArrayList<>();

    public WizardStepModel(String key, String label)
    {
        this.key = key;
        this.label = label;
    }

    public String getKey()
    {
        return key;
    }

    public String getLabel()
    {
        return label;
    }

    public List<WizardTypeModel> getTypes()
    {
        return types;
    }

    public void addType(WizardTypeModel type)
    {
        types.add(type);
    }
}
