package com.zutubi.tove.ui.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A wizard step to configure a defined type.  If the type is concrete, a common case, there is
 * only one entry in the types list. Otherwise the user has a choice of types, effectively making
 * this two steps (choice of type and config of that type).
 */
@JsonTypeName("typed")
public class TypedWizardStepModel extends WizardStepModel
{
    private List<WizardTypeModel> types = new ArrayList<>();
    private String defaultType;

    public TypedWizardStepModel(String key, String label)
    {
        super(label, key);
    }

    public boolean hasType(String symbolicName)
    {
        for (WizardTypeModel type: types)
        {
            if (Objects.equals(type.getType().getSymbolicName(), symbolicName))
            {
                return true;
            }
        }

        return false;
    }

    public List<WizardTypeModel> getTypes()
    {
        return types;
    }

    public void addType(WizardTypeModel type)
    {
        types.add(type);
    }

    public String getDefaultType()
    {
        return defaultType;
    }

    public void setDefaultType(String defaultType)
    {
        this.defaultType = defaultType;
    }
}
