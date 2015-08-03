package com.zutubi.pulse.master.rest.model.forms;

import com.zutubi.tove.annotations.FieldType;

/**
 * Represents a checkbox field in a form.
 */
public class CheckboxFieldModel extends FieldModel
{
    public CheckboxFieldModel()
    {
        setType(FieldType.CHECKBOX);
        setSubmitOnEnter(true);
    }
}
