package com.zutubi.tove.type;

import com.zutubi.tove.annotations.ReadOnly;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * An implementation of the TypeProperty that for a compile type defined type property.
 * <p/>
 * A property is defined as a field, named 'x', with accessors getX and setX.
 * <p/>
 * A public field is considered read and writeable.  A private field is considered readable
 * if it has a public getter, and writeable if it has a public setter.
 */
public class DefinedTypeProperty extends TypeProperty
{
    private Field field;

    private Method getter;

    private Method setter;

    protected void setField(Field field)
    {
        this.field = field;
    }

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
        Object result = null;
        if (getter != null)
        {
            result = getter.invoke(instance);
        }
        else if (field != null)
        {
            boolean accessible = field.isAccessible();
            try
            {
                field.setAccessible(true);
                result = field.get(instance);
            }
            finally
            {
                field.setAccessible(accessible);
            }
        }

        if (getType() instanceof PrimitiveType)
        {
            PrimitiveType primitiveType = (PrimitiveType) getType();
            Object nullValue = primitiveType.getNullValue();
            if (nullValue != null && nullValue.equals(result))
            {
                return null;
            }
        }

        return result;
    }

    public void setValue(Object instance, Object value) throws TypeException
    {
        try
        {
            if (value == null && getType() instanceof PrimitiveType)
            {
                PrimitiveType primitiveType = (PrimitiveType) getType();
                value = primitiveType.getNullValue();
            }

            // if a setter is defined, then it will be expected that the setter is used.  In the case that
            // the setter fails, we give up since to go direct to the field would be unexpected.
            if (setter != null)
            {
                setter.invoke(instance, value);
            }
            else if (field != null)
            {
                // should we bother resetting the accessible field flag?
                boolean accessible = field.isAccessible();
                try
                {
                    field.setAccessible(true);
                    field.set(instance, value);
                }
                finally
                {
                    field.setAccessible(accessible);
                }
            }
            else
            {
                // should we ever end up in this sort of situation?.
            }
        }
        catch (Exception e)
        {
            throw new TypeException("Failed to set property '" + getName() + "': " + e.getMessage(), e);
        }
    }

    public boolean isReadable()
    {
        return getter != null && Modifier.isPublic(getter.getModifiers()) || field != null && Modifier.isPublic(field.getModifiers());
    }

    public boolean isWriteable()
    {
        // annotation overrides all.
        if (getAnnotation(ReadOnly.class) != null)
        {
            return false;
        }

        // public setter or public field.
        return setter != null && Modifier.isPublic(setter.getModifiers()) || field != null && Modifier.isPublic(field.getModifiers());
    }
}
