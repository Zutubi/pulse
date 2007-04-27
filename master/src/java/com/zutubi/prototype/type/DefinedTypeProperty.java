package com.zutubi.prototype.type;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 *
 *
 */
public class DefinedTypeProperty extends TypeProperty
{
    private Method getter;

    private Method setter;

    protected void setGetter(Method getter)
    {
        this.getter = getter;
    }

    protected void setSetter(Method setter)
    {
        this.setter = setter;
    }

    public Object getValue(Object instance) throws IllegalAccessException, InvocationTargetException
    {
        if (getter != null)
        {
            return getter.invoke(instance);
        }
        return null;
    }

    public void setValue(Object instance, Object value) throws IllegalAccessException, InvocationTargetException
    {
        // This is a read only property.
        if (setter == null)
        {
            return;
        }

        // can not set nulls on primitive types (int, long, bool, etc), so we treat null by
        // leaving the default value.
        if (value != null || !(getType() instanceof PrimitiveType))
        {
            setter.invoke(instance, value);
        }
    }

    public boolean isReadable()
    {
        return getter != null;
    }

    public boolean isWriteable()
    {
        return setter != null;
    }
}
