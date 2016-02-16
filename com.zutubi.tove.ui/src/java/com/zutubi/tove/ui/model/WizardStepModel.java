package com.zutubi.tove.ui.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract base for steps of a wizard.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(TypedWizardStepModel.class),
        @JsonSubTypes.Type(CustomWizardStepModel.class)
})
public abstract class WizardStepModel
{
    private String key;
    private String label;

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
