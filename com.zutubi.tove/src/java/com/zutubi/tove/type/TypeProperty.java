package com.zutubi.tove.type;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public abstract class TypeProperty
{
    /**
     * Annotations associated with this property.  This includes annotations
     * directly on the property, on the property in supertypes, and meta-
     * annotations recursively.
     */
    private List<Annotation> annotations = new LinkedList<Annotation>();

    /**
     * The name of this property.
     */
    private String name;

    /**
     * The type of this property.
     */
    private Type type;

    public TypeProperty()
    {
    }

    public TypeProperty(String name)
    {
        this.name = name;
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
        return type.cast(CollectionUtils.find(annotations, new Predicate<Annotation>()
        {
            public boolean satisfied(Annotation annotation)
            {
                return annotation.annotationType() == type;
            }
        }));
    }

    public String getName()
    {
        return name;
    }

    protected void setName(String name)
    {
        this.name = name;
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

    public abstract Object getValue(Object instance) throws Exception;
    
    public abstract void setValue(Object instance, Object value) throws TypeException;

    public abstract boolean isReadable();
    
    public abstract boolean isWritable();
}
