package com.zutubi.pulse.master.rest.model.forms;

import com.zutubi.tove.annotations.FieldType;

import java.util.List;

/**
 * Field used for rendering dropdown lists.
 */
public class DropdownFieldModel extends OptionFieldModel
{
    public DropdownFieldModel()
    {
        setType(FieldType.DROPDOWN);
    }

    public DropdownFieldModel(String name, String label, List list)
    {
        super(FieldType.DROPDOWN, name, label, list);
    }
}
