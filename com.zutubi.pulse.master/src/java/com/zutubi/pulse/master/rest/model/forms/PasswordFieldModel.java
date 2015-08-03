package com.zutubi.pulse.master.rest.model.forms;

import com.zutubi.tove.annotations.FieldType;

/**
 * Models a text entry field that does not display its contents.
 */
public class PasswordFieldModel extends TextFieldModel
{
    public PasswordFieldModel()
    {
        setType(FieldType.PASSWORD);
    }
}
