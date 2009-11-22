package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.annotations.FieldType;

/**
 */
public class ItemPickerFieldDescriptor extends OptionFieldDescriptor
{
    public static final String PARAMETER_SUPPRESS_DEFAULT = "suppressDefault";

    public ItemPickerFieldDescriptor()
    {
        setType(FieldType.ITEM_PICKER);
    }
}
