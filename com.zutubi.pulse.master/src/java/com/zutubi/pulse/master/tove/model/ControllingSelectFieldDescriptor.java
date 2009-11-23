package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.annotations.FieldType;

/**
 * Describes a controlling select: i.e. a select whose selected value
 * controls the enabled state of other fields.
 */
public class ControllingSelectFieldDescriptor extends OptionFieldDescriptor
{
    public static final String PARAM_ENABLE_SET       = "enableSet";
    public static final String PARAM_DEPENDENT_FIELDS = "checkedFields";

    public ControllingSelectFieldDescriptor()
    {
        setType(FieldType.CONTROLLING_SELECT);
        setSubmitOnEnter(true);
    }

    public void setEnableSet(String[] values)
    {
        addParameter(PARAM_ENABLE_SET, values);
    }

    public void setDependentFields(String[] deps)
    {
        addParameter(PARAM_DEPENDENT_FIELDS, deps);
    }

    protected boolean transformType()
    {
        return false;
    }
}
