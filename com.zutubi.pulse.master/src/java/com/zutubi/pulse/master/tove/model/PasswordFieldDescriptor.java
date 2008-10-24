package com.zutubi.pulse.master.tove.model;

import com.zutubi.pulse.master.tove.model.FieldDescriptor;

/**
 *
 *
 */
public class PasswordFieldDescriptor extends FieldDescriptor
{
    public PasswordFieldDescriptor()
    {
        setType("password");
        setSubmitOnEnter(true);
    }
}
