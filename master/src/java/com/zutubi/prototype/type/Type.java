package com.zutubi.prototype.type;

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

    Class getClazz();

    /**
     * Returns an instance of the object defined by this type, using the record object to populate the details.
     *
     * @param record
     *
     * @return
     * 
     * @throws TypeException
     */
    Object instantiate(Object record) throws TypeException;

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
}
