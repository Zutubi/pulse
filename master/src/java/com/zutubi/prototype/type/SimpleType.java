package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.RecordUtils;

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

    public boolean deepValueEquals(Object data1, Object data2)
    {
        return RecordUtils.valuesEqual(data1, data2);
    }

    public void initialise(Object instance, Object data, Instantiator instantiator)
    {
        // Nothing to to
    }

    public abstract String fromXmlRpc(Object data) throws TypeException;

    public String toString()
    {
        return getClazz().getSimpleName().toLowerCase();
    }
}
