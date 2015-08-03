package com.zutubi.pulse.master.rest.model.forms;

import com.zutubi.tove.annotations.FieldType;

/**
 * Field used for rendering comboboxes and dropdowns.
 */
public class SelectFieldModel extends OptionFieldModel
{
    public SelectFieldModel()
    {
        setType(FieldType.SELECT);
        setLazy(false);
    }
}
