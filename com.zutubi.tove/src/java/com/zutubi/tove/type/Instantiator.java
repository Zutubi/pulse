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

import com.zutubi.tove.config.api.Configuration;

/**
 * A callback interface used by types to instantiate their properties (or
 * child items in the case of a collection).  This allows extra logic to be
 * performed on the instances, such as caching, without the knowledge of the
 * types.
 */
public interface Instantiator
{
    /**
     * Instantiates the given property using the type and data provided.
     * Errors are recorded on the instantiated instance, except where the
     * error prevents the instance creation.
     *
     * @param path     path of the value being instantiated
     * @param relative if true, the path is relative to the current instance,
     *                 otherwise the path is absolute
     * @param type     actual type of the property
     * @param data     the property data (e.g. record, string)
     * @return the instance
     *
     * @throws TypeException if an error prevents instance creation
     */
    Object instantiate(String path, boolean relative, Type type, Object data) throws TypeException;

    /**
     * Resolves a reference given the referred-to handle.  The referred-to
     * object may be instantiated on demand in some circumstances.
     *
     * @param toHandle the handle of the referenced object
     * @return the reference object
     * @throws TypeException if the reference cannot be resolved
     */
    Configuration resolveReference(long toHandle) throws TypeException;
}
