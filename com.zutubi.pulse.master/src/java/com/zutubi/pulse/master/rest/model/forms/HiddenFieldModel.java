package com.zutubi.pulse.master.rest.model.forms;

import com.zutubi.tove.annotations.FieldType;

/**
 * A field that is not displayed, just used to carry a value to be submitted.
 */
public class HiddenFieldModel extends FieldModel
{
    public HiddenFieldModel()
    {
        setType(FieldType.HIDDEN);
    }
}
