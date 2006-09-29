package com.zutubi.pulse.form.persist;

import java.lang.reflect.Method;

/**
 * <class-comment/>
 */
public interface PersistenceDescriptor
{
    public String getIdProperty();

    public void setIdProperty(String propertyName);

    public Method getWriterMethod();

    public Method getReaderMethod();

    void setReaderMethod(Method readerMethod);

    void setWriterMethod(Method writerMethod);
}
