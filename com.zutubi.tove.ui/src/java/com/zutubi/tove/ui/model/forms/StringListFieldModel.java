package com.zutubi.tove.ui.model.forms;

import com.zutubi.tove.annotations.FieldType;

/**
 * A field that lets the user configure a list of arbitrary strings.
 */
public class StringListFieldModel extends FieldModel
{
    public StringListFieldModel()
    {
        setType(FieldType.STRING_LIST);
    }
}
