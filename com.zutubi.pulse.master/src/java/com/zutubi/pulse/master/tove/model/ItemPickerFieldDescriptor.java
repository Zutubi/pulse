package com.zutubi.pulse.master.tove.model;

import com.zutubi.config.annotations.FieldType;

/**
 */
public class ItemPickerFieldDescriptor extends OptionFieldDescriptor
{
    public ItemPickerFieldDescriptor()
    {
        setType(FieldType.ITEM_PICKER);
    }
}
