package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Predicate;

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

    public <T extends Annotation> T getAnnotation(final Class<T> type)
    {
        return (T) CollectionUtils.find(annotations, new Predicate<Annotation>()
        {
            public boolean satisfied(Annotation annotation)
            {
                return annotation.getClass() == type;
            }
        });
    }

    public <T extends Annotation> List<T> getAnnotations(final Class<T> type)
    {
        return (List<T>) CollectionUtils.filter(annotations, new Predicate<Annotation>()
        {
            public boolean satisfied(Annotation annotation)
            {
                return annotation.getClass() == type;
            }
        });
    }

    void addAnnotations(List<Annotation> annotations)
    {
        this.annotations.addAll(annotations);
    }
}
