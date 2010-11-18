package com.zutubi.pulse.master.tove.model;

import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.type.record.Record;

/**
 * Descriptor for a text field that hides its contents - suitable for entering
 * sensitive values like passwords.
 */
public class PasswordFieldDescriptor extends FieldDescriptor
{
    public PasswordFieldDescriptor()
    {
        setType("password");
        setSubmitOnEnter(true);
    }

    @Override
    public Field instantiate(String path, Record instance)
    {
        Field field = super.instantiate(path, instance);
        Object value = field.getValue();
        if (value != null && value instanceof String && ((String) value).length() > 0)
        {
            field.setValue(ToveUtils.SUPPRESSED_PASSWORD);
        }
        return field;
    }
}
