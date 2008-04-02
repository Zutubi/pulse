package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper base class that takes care of some of the type implementation.
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

    public <T extends Annotation> boolean hasAnnotation(Class<T> annotationType)
    {
        return getAnnotation(annotationType) != null;
    }

    public <T extends Annotation> T getAnnotation(final Class<T> annotationType)
    {
        return (T) CollectionUtils.find(annotations, new Predicate<Annotation>()
        {
            public boolean satisfied(Annotation annotation)
            {
                return annotation.annotationType().equals(annotationType);
            }
        });
    }

    public Type getTargetType()
    {
        return this;
    }

    public Type getActualType(Object value)
    {
        return this;
    }

    public void addAnnotation(Annotation annotation)
    {
        this.annotations.add(annotation);
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    protected static void typeCheck(Object data, Class expectedClass) throws TypeException
    {
        if(!expectedClass.isInstance(data))
        {
            throw new TypeException("Expecting '" + expectedClass.getName() + "', found '" + data.getClass().getName() + "'");
        }
    }

    protected void copyMetaToRecord(Object instance, MutableRecord record)
    {
        if(instance instanceof Configuration)
        {
            Configuration configuration = (Configuration) instance;
            for(String key: configuration.metaKeySet())
            {
                record.putMeta(key, configuration.getMeta(key));
            }
        }
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
