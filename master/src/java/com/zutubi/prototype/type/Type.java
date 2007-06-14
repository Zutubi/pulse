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

    Type getTargetType();

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
     * @param data is the data that represents this type.  The type of the data will vary based on type
     * implementation.  For instance, for a <code>PrimitiveType</code>, the data will be a primitive java type.  For a
     * <code>CompositeType</code>, the data will be a <code>Record</code> instance.
     * @param path defines the path defining the data to be instantiated.
     *
     * @return the instance defined by this type and data.
     *
     * @throws TypeException if there is a problem instantiating the data.
     */
    //TODO: This is a little awkward, we ask the client to provide both the data AND the path. We should try to
    //TODO: take the path out of this interface since it introduces caching responsibilities to the type.  The
    //TODO: awkwardness is sort of required however to allow the instantiation of nested objects.  We would need
    //TODO: to externalise the nested instantiation - not too difficult.
    Object instantiate(String path, Object data) throws TypeException;

    Object unstantiate(Object instance) throws TypeException;
}
