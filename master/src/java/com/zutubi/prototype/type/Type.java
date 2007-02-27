package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;

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

    List<Annotation> getAnnotations(Class annotationType);

    Annotation getAnnotation(Class annotationType);

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

    /**
     * Get a list of this types properties.
     * 
     * @return
     */
    List<TypeProperty> getProperties();

    /**
     * Get a list of this types property names.
     *
     * @return
     */
    List<String> getPropertyNames();

    /**
     * Retrieve this types named property.
     *
     * @param name
     *
     * @return
     */
    TypeProperty getProperty(String name);

    /**
     * Retrieve a list of properties that are of the specified type.
     *
     * @param type
     * 
     * @return
     */
    List<TypeProperty> getProperties(Class<? extends Type> type);

    /**
     * Retrive a list of property names for all properties of the specified type.
     *
     * @param type
     * 
     * @return
     */
    List<String> getPropertyNames(Class<? extends Type> type);

    /**
     * Returns true if this type has a property with the specified name.
     * @param propertyName
     * @return
     */
    boolean hasProperty(String propertyName);

    void setRecord(String path, Record record, RecordManager recordManager);
}
