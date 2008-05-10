package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.GraphFunction;

/**
 */
public interface ComplexType extends Type
{
    String getSymbolicName();

    MutableRecord createNewRecord(boolean applyDefaults);

    boolean isTemplated();

    Type getDeclaredPropertyType(String propertyName);

    Type getActualPropertyType(String propertyName, Object propertyValue);

    /**
     * Checks if the given instance is transitively valid.  Contrast this to
     * {@link Configuration#isValid}, which only checks the instance itself,
     * ignoring nested instances.
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
}
