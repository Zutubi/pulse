package com.zutubi.pulse.master.rest.model.forms;

import com.zutubi.tove.annotations.FieldType;

/**
 * Field used for rendering editable dropdown lists.
 */
public class ComboboxFieldModel extends OptionFieldModel
{
    public ComboboxFieldModel()
    {
        setType(FieldType.COMBOBOX);
    }
}
