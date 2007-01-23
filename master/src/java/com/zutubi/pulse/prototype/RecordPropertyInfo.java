package com.zutubi.pulse.prototype;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Stores information about a single property from a record type.
 */
public class RecordPropertyInfo
{
    private String name;
    private Type type;
    private Method getter;
    private Method setter;

    public RecordPropertyInfo(String name, Method getter, Method setter)
    {
        this.name = name;
        this.type = getter.getGenericReturnType();
        this.getter = getter;
        this.setter = setter;
    }

    public String getName()
    {
        return name;
    }

    public Type getType()
    {
        return type;
    }

    public Method getGetter()
    {
        return getter;
    }

    public Method getSetter()
    {
        return setter;
    }
}
