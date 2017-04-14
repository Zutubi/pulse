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

package com.zutubi.tove.variables.api;

import java.util.Collection;

/**
 * A container for variables which can be looked up by name.
 */
public interface VariableMap
{
    /**
     * Returns true if this map contains a variable of the specified name.
     *
     * @param name the name of the variable.
     * @return true if this instance contains the variable, false otherwise.
     */
    boolean containsVariable(String name);

    /**
     * Get the named variable.
     *
     * @param name the name of the variable
     * @return the variable, or null if this map does not contain the variable.
     */
    Variable getVariable(String name);

    /**
     * Return all of the variables contained by this map as a collection.
     *
     * @return a collection of all the variable.
     */
    Collection<Variable> getVariables();

}
