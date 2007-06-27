package com.zutubi.prototype.type;

import com.zutubi.prototype.config.InstanceCache;

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

    Type getTargetType();

    Type getActualType(Object value);

    /**
     * The underlying class represented by this type instance.
     *
     * @return
     */
    Class getClazz();

    /**
     * Returns an instance of the object defined by this type, using the data to populate the details.
     * <p/>
     * The type of the data will vary between types.
     *
     * @param path defines the path defining the data to be instantiated, may be null
     * @param cache cache that all new instances should be inserted into, ignored
     *              when path is null
     * @param data is the data that represents this type.  The type of the data will vary based on type
     * implementation.  For instance, for a <code>PrimitiveType</code>, the data will be a primitive java type.  For a
     * <code>CompositeType</code>, the data will be a <code>Record</code> instance.
     * @return the instance defined by this type and data.
     *
     * @throws TypeException if there is a problem instantiating the data.
     */
    Object instantiate(String path, InstanceCache cache, Object data) throws TypeException;

    Object unstantiate(Object instance) throws TypeException;
}
