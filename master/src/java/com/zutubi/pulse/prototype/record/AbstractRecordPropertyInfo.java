package com.zutubi.pulse.prototype.record;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores information about a single property from a record type.
 */
public abstract class AbstractRecordPropertyInfo implements RecordPropertyInfo
{
    private String name;
    private Type type;
    private Method getter;
    private Method setter;
    private List<Annotation> annotations = new LinkedList<Annotation>();

    public AbstractRecordPropertyInfo(String name, Method getter, Method setter)
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

    public List<Annotation> getAnnotations()
    {
        return annotations;
    }

    void addAnnotations(List<Annotation> annotations)
    {
        this.annotations.addAll(annotations);
    }
}
