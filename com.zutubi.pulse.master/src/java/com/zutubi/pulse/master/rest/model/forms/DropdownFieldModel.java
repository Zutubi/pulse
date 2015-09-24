package com.zutubi.pulse.master.rest.model.forms;

import com.zutubi.tove.annotations.FieldType;

/**
 * Field used for rendering dropdown lists.
 */
public class DropdownFieldModel extends OptionFieldModel
{
    public DropdownFieldModel()
    {
        setType(FieldType.DROPDOWN);
    }
}
