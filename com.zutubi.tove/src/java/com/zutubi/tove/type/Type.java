package com.zutubi.tove.type;

/**
 *
 *
 */
public interface Type
{
    Type getTargetType();

    Type getActualType(Object value);

    /**
     * @return the underlying class represented by this type instance
     */
    Class getClazz();

    boolean deepValueEquals(Object data1, Object data2);

    /**
     * Returns an instance of the object defined by this type, based on the
     * given data.  Note that the instance will not be initialised with child
     * properties.  Use {@link #initialise} to fill in the instance.
     *
     * @param data the data that represents this type.  The type of the data will vary based on type
     *             implementation.  For example, for a <code>PrimitiveType</code>, the data will be
     *             a primitive java type.  For a <code>CompositeType</code>, the data will be a
     *             <code>Record</code> instance.
     * @param instantiator callback interface used during
     *                             instantiation
     * @return the instance defined by this type and data.
     *
     * @throws TypeException if there is a problem creating the instance
     */
    Object instantiate(Object data, Instantiator instantiator) throws TypeException;

    /**
     * Fills in the properties of the given instance with the given data.
     * The instance should have been previously created with {@link
     * #instantiate}.  Errors are recorded on the instance.
     *
     * @param instance             instance to be initialised
     * @param data                 data to initialise the instance with
     * @param instantiator callback interface used to instantiate child properties
     */
    void initialise(Object instance, Object data, Instantiator instantiator);

    /**
     * Converts from an instance back to record form (i.e. records, strings,
     * etc).  The inverse of {@link #instantiate}.
     *
     * @param instance the instance to convert, may be null
     * @return record form of the instance, may be null if there is no
     *         representation for the given instance
     * @throws TypeException in the event of an error
     */
    Object unstantiate(Object instance) throws TypeException;

    /**
     * Converts the given record form data into a form suitable for use in an
     * XML-RPC call.  Records are converted to hashtables, strings to
     * XML-RPC types (where sensible) and so on.
     *
     * @param templateOwnerPath path of the templated collection item that
     *                          contains the data, may be null if not within a
     *                          templated scope
     * @param data              the data to be converted (record form)
     * @return the XML-RPC formatted data
     * @throws TypeException in the event of an error
     */
    Object toXmlRpc(String templateOwnerPath, Object data) throws TypeException;

    /**
     * Converts the given XML-RPC form data into record form.  This is the
     * inverse of {#link toXmlRpc}.
     *
     * @param templateOwnerPath path of the templated collection item that
     *                          the data is or will be contained by, may be
     *                          null if not within a templated scope
     * @param data the data to be converted (XML-RPC form)  @return the record formatted data
     * @throws TypeException in the event of an error
     */
    Object fromXmlRpc(String templateOwnerPath, Object data) throws TypeException;
}
