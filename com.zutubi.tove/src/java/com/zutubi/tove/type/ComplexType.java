package com.zutubi.tove.type;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.util.GraphFunction;

/**
 */
public interface ComplexType extends Type
{
    String getSymbolicName();

    MutableRecord createNewRecord(boolean applyDefaults);

    boolean isTemplated();

    Type getDeclaredPropertyType(String propertyName);

    /**
     * Gets the actual (as opposed to declared) type of the property with the given name and value.
     * If there is no value the declared type is returned.
     * 
     * @param propertyName  name of the property to get the type of
     * @param propertyValue value of the property, may be null
     * @return the type of the named property's current value, or the declared type if it is unset
     */
    Type getActualPropertyType(String propertyName, Object propertyValue);

    /**
     * Checks if the given instance is transitively valid.  Contrast this to
     * {@link com.zutubi.tove.config.api.Configuration#isValid}, which only checks the instance
     * itself, ignoring nested instances.
     *
     * @param instance the instance to check
     * @return true if the given instance is valid in the transitive sense
     *         (i.e. there are no errors on the instance or any nested
     *         instances).
     */
    boolean isValid(Object instance);

    /**
     * Performs a type-directed walk of an instance graph, calling the given
     * function on each complex object in the graph.
     *
     * @param instance root of the object graph to walk
     * @param f        function to call
     * @throws TypeException if an error occurs walking the graph
     */
    void forEachComplex(Object instance, GraphFunction<Object> f) throws TypeException;

    boolean hasSignificantKeys();
}
