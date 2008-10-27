package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.annotations.FieldType;
import com.zutubi.pulse.master.tove.model.FieldDescriptor;

/**
 * Describes a controlling checkbox: i.e. a checkbox whose checked state
 * controls the enabled state of other fields.
 */
public class ControllingCheckboxFieldDescriptor extends FieldDescriptor
{
    public static final String PARAM_INVERT           = "invert";
    public static final String PARAM_DEPENDENT_FIELDS = "dependentFields";

    public ControllingCheckboxFieldDescriptor()
    {
        setType(FieldType.CONTROLLING_CHECKBOX);
        setSubmitOnEnter(true);
    }

    public void setInvert(boolean invert)
    {
        addParameter(PARAM_INVERT, invert);
    }

    public void setDependentFields(String[] deps)
    {
        addParameter(PARAM_DEPENDENT_FIELDS, deps);
    }
}
