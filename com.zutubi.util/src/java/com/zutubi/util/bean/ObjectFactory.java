package com.zutubi.util.bean;

/**
 * Interface for a generic factory, used to build instances by type.
 */
public interface ObjectFactory
{
    /**
     * Creates an instance of the given class using the default constructor.
     *
     * @param clazz the type to construct an instance of
     * @param <T> the type of the returned insstance
     * @return the new instance
     * @throws RuntimeException on error
     */
    public <T> T buildBean(Class<? extends T> clazz);

    /**
     * Creates an instance of the given named class using the default,
     * constructor and casts it to the given type.
     *
     * @param className name of the class to construct an instance of
     * @param supertype type to cast the instance to
     * @param <T> the type of the returned insstance
     * @return the new instance
     * @throws RuntimeException on error
     */
    public <T> T buildBean(String className, Class<? super T> supertype);

    /**
     * Creates an instance of the given class using a constructor with the
     * given argument types.
     *
     * @param clazz    the type to construct an instance of
     * @param argTypes types of the constructor arguments
     * @param args     arguments to pass to the constructor (must match the
     *                 types)
     * @param <T> the type of the returned instance
     * @return the new instance
     * @throws RuntimeException on error
     */
    <T> T buildBean(Class<? extends T> clazz, Class[] argTypes, Object[] args);

    /**
     * Creates an instance of the given named class using a constructor with
     * the given argument types.  The result is cast to the given type.
     *
     * @param className name of the class to construct an instance of
     * @param supertype type to cast the instance to
     * @param argTypes  types of the constructor arguments
     * @param args      arguments to pass to the constructor (must match the
     *                  types)
     * @param <T> the type of the returned insstance
     * @return the new instance
     * @throws RuntimeException on error
     */
    <T> T buildBean(String className, Class<? super T> supertype, Class[] argTypes, Object[] args);

    /**
     * Loads the class of the given name, ensuring it can be assigned to the
     * token type.
     *
     * @param className name of the class to load
     * @param supertype type that the class must be a subtype of
     * @param <T>       the type that the returned class represents
     * @return the loaded class
     */
    <T> Class<? extends T> getClassInstance(String className, Class<? super T> supertype);
}
