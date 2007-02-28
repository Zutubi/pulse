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
     * <p/>
     * The type of the record will vary between types.
     *
     * @param record
     * @return
     * @throws TypeException
     */
    Object instantiate(Object record) throws TypeException;
}
