/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.tove.type;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.util.GraphFunction;

/**
 */
public interface ComplexType extends Type
{
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
