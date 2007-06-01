package com.zutubi.prototype.model;

import com.zutubi.config.annotations.FieldType;
import com.zutubi.prototype.FieldDescriptor;

/**
 * Describes a controlling checkbox: i.e. a checkbox whose checked state
 * controls the enabled state of other fields.
 */
public class ControllingCheckboxFieldDescriptor extends FieldDescriptor
{
    public ControllingCheckboxFieldDescriptor()
    {
        setType(FieldType.CONTROLLING_CHECKBOX);
        setSubmitOnEnter(true);
    }

    public void setInvert(boolean invert)
    {
        addParameter("invert", invert);
    }

    public void setDependentFields(String[] deps)
    {
        addParameter("dependentFields", deps);
    }
}
