package com.zutubi.tove.model;

import com.zutubi.tove.FieldDescriptor;

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
