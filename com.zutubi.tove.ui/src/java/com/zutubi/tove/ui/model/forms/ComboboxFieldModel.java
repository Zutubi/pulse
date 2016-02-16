package com.zutubi.tove.ui.model.forms;

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
