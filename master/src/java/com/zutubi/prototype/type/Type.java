package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 *
 */
public interface Type
{
    /**
     * Retrieve a list of the annotations defined on this type.
     *
     * @return a list of annotations.
     */
    List<Annotation> getAnnotations();

    /**
     * The underlying class represented by this type instance.
     *
     * @return
     */
    Class getClazz();

    /**
     * Returns an instance of the object defined by this type, using the record to populate the details.
     *
     * The type of the record will vary between types. 
     *
     * @param record
     *
     * @return
     * 
     * @throws TypeException
     */
    Object instantiate(Object record) throws TypeException;

    Object instantiate() throws TypeConversionException;

    /**
     * Returns a version of the instance that have been converted for persistence purposes.
     *
     * @param instance
     * 
     * @return
     *
     * @throws TypeException
     */
    Object unstantiate(Object instance) throws TypeException;

    String getSymbolicName();

    List<TypeProperty> getProperties();

    List<String> getPropertyNames();

    TypeProperty getProperty(String name);

    List<TypeProperty> getProperties(Class<? extends Type> type);

    List<String> getPropertyNames(Class<? extends Type> type);

    boolean hasProperty(String propertyName);
}
