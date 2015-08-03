package com.zutubi.pulse.master.rest.model.forms;

import com.zutubi.tove.annotations.FieldType;

/**
 * Describes a controlling select: i.e. a select whose selected value controls the enabled state of
 * other fields.
 */
public class ControllingSelectFieldModel extends OptionFieldModel
{
    private String[] enableSet;
    private String[] dependentFields;

    public ControllingSelectFieldModel()
    {
        setType(FieldType.CONTROLLING_SELECT);
        setSubmitOnEnter(true);
    }

    public String[] getEnableSet()
    {
        return enableSet;
    }

    public void setEnableSet(String[] enableSet)
    {
        this.enableSet = enableSet;
    }

    public String[] getDependentFields()
    {
        return dependentFields;
    }

    public void setDependentFields(String[] dependentFields)
    {
        this.dependentFields = dependentFields;
    }

    protected boolean transformType()
    {
        return false;
    }
}
