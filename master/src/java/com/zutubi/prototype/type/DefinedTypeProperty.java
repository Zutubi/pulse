package com.zutubi.prototype.type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
            Object o = getter.invoke(instance);
            if(getType() instanceof PrimitiveType)
            {
                PrimitiveType primitiveType = (PrimitiveType) getType();
                Object nullValue = primitiveType.getNullValue();
                if(nullValue != null && nullValue.equals(o))
                {
                    return null;
                }
            }
            
            return o;
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

        if(value == null && getType() instanceof PrimitiveType)
        {
            PrimitiveType primitiveType = (PrimitiveType) getType();
            value = primitiveType.getNullValue();
        }

        setter.invoke(instance, value);
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
