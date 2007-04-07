package com.zutubi.prototype.type;

/**
 * Simple types are stored as strings and editable as fields in a form.
 */
public abstract class SimpleType extends AbstractType
{
    public SimpleType(Class clazz)
    {
        super(clazz);
    }

    public SimpleType(Class type, String symbolicName)
    {
        super(type, symbolicName);
    }
}
