package com.zutubi.pulse.master.rest.model.forms;

import com.zutubi.tove.annotations.FieldType;

/**
 * An item picker is a type of multi-select control that shows the selected set in one box, and a
 * list of options in another.
 */
public class ItemPickerFieldModel extends OptionFieldModel
{
    public static final String PARAMETER_SUPPRESS_DEFAULT = "suppressDefault";

    public ItemPickerFieldModel()
    {
        setType(FieldType.ITEM_PICKER);
    }
}
