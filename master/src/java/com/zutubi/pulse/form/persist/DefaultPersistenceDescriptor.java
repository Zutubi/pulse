package com.zutubi.pulse.form.persist;

import com.zutubi.pulse.form.persist.PersistenceDescriptor;

import java.lang.reflect.Method;

/**
 * <class-comment/>
 */
public class DefaultPersistenceDescriptor implements PersistenceDescriptor
{
    private String idProperty;

    private Method readerMethod;
    private Method writerMethod;

    public String getIdProperty()
    {
        return idProperty;
    }

    public void setIdProperty(String idProperty)
    {
        this.idProperty = idProperty;
    }

    public Method getReaderMethod()
    {
        return readerMethod;
    }

    public void setReaderMethod(Method readerMethod)
    {
        this.readerMethod = readerMethod;
    }

    public Method getWriterMethod()
    {
        return writerMethod;
    }

    public void setWriterMethod(Method writerMethod)
    {
        this.writerMethod = writerMethod;
    }
}
