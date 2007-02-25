package com.zutubi.prototype.type;

import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Predicate;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public abstract class AbstractType implements Type
{
    protected TypeRegistry typeRegistry;

    private List<Annotation> annotations = new LinkedList<Annotation>();

    private Class clazz;
    private String symbolicName;

    public AbstractType(Class clazz)
    {
        this(clazz, null);
    }

    public AbstractType(Class type, String symbolicName)
    {
        this.clazz = type;
        this.symbolicName = symbolicName;
    }

    public Class getClazz()
    {
        return clazz;
    }

    public void setAnnotations(List<Annotation> annotations)
    {
        this.annotations = new LinkedList<Annotation>(annotations);
    }

    public List<Annotation> getAnnotations()
    {
        return Collections.unmodifiableList(annotations);
    }

    public List<Annotation> getAnnotations(final Class annotationType)
    {
        return CollectionUtils.filter(annotations, new Predicate<Annotation>()
        {
            public boolean satisfied(Annotation annotation)
            {
                return annotation.annotationType().equals(annotationType);
            }
        });
    }

    public Annotation getAnnotation(final Class annotationType)
    {
        return CollectionUtils.find(annotations, new Predicate<Annotation>()
        {
            public boolean satisfied(Annotation annotation)
            {
                return annotation.annotationType().equals(annotationType);
            }
        });
    }

    public void addAnnotation(Annotation annotation)
    {
        this.annotations.add(annotation);
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }


    public List<TypeProperty> getProperties()
    {
        return Collections.EMPTY_LIST;
    }

    public TypeProperty getProperty(String name)
    {
        return null;
    }

    public List<TypeProperty> getProperties(Class<? extends Type> type)
    {
        return Collections.EMPTY_LIST;
    }

    public List<String> getPropertyNames()
    {
        return Collections.EMPTY_LIST;
    }

    public List<String> getPropertyNames(Class<? extends Type> type)
    {
        return Collections.EMPTY_LIST;
    }

    public boolean hasProperty(String propertyName)
    {
        return false;
    }
}
