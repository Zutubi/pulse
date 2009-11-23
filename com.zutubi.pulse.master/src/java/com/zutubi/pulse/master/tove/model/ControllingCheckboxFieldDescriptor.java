package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.annotations.FieldType;

/**
 * Describes a controlling checkbox: i.e. a checkbox whose checked state
 * controls the enabled state of other fields.
 */
public class ControllingCheckboxFieldDescriptor extends FieldDescriptor
{
    public static final String PARAM_CHECKED_FIELDS   = "checkedFields";
    public static final String PARAM_UNCHECKED_FIELDS = "uncheckedFields";

    public ControllingCheckboxFieldDescriptor()
    {
        setType(FieldType.CONTROLLING_CHECKBOX);
        setSubmitOnEnter(true);
    }

    public void setCheckedFields(String[] deps)
    {
        addParameter(PARAM_CHECKED_FIELDS, deps);
    }

    public void setUncheckedFields(String[] deps)
    {
        addParameter(PARAM_UNCHECKED_FIELDS, deps);
    }
}
