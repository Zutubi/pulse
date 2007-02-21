package com.zutubi.prototype.type;

import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Predicate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class TypeProperty
{
    private List<Annotation> annotations = new LinkedList<Annotation>();

    private String name;
    private Method getter;
    private Method setter;
    private Type type;

    public TypeProperty()
    {
    }

    public TypeProperty(String name, Method getter, Method setter)
    {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    public TypeProperty(String name, Type type)
    {
        this.name = name;
        this.type = type;
    }

    public List<Annotation> getAnnotations()
    {
        return annotations;
    }

    protected void setAnnotations(List<Annotation> annotations)
    {
        this.annotations = annotations;
    }

    protected void addAnnotation(Annotation annotation)
    {
        this.annotations.add(annotation);
    }

    public <T extends Annotation> T getAnnotation(final Class<T> type)
    {
        return (T) CollectionUtils.find(annotations, new Predicate<Annotation>()
        {
            public boolean satisfied(Annotation annotation)
            {
                return annotation.annotationType() == type;
            }
        });
    }

    public <T extends Annotation> List<T> getAnnotations(final Class<T> type)
    {
        return (List<T>) CollectionUtils.filter(annotations, new Predicate<Annotation>()
        {
            public boolean satisfied(Annotation annotation)
            {
                return annotation.annotationType() == type;
            }
        });
    }
    
    public String getName()
    {
        return name;
    }

    protected void setName(String name)
    {
        this.name = name;
    }

    public Method getGetter()
    {
        return getter;
    }

    protected void setGetter(Method getter)
    {
        this.getter = getter;
    }

    public Method getSetter()
    {
        return setter;
    }

    protected void setSetter(Method setter)
    {
        this.setter = setter;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public Class getClazz()
    {
        return type.getClazz();
    }
}
