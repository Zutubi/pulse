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

package com.zutubi.pulse.master.util.cache;

/**
 * The Cache abstracts the cache implementation.
 *
 */
public interface Cache
{
    /**
     * The name of the cache uniquely identifies this cache.
     *
     * @return the name of the cache.
     */
    String getName();

    /**
     * Retrieve an object from this cache.
     * @param key uniquely identifying the object to be retrieved.
     *
     * @return the object from the cache, or null if the object is not found.
     */
    Object get(Object key);

    /**
     * Put an object into the cache. If the specified key already exists within the cache, it will be replaced by
     * the new object.
     *
     * @param key uniquely identifying the object to be added into the cache.
     * @param value to be cached.
     */
    void put(Object key, Object value);

    /**
     * Remove the object identified by the key from the cache. If no object can be found associated with this key
     * then no action is taken.
     *
     * @param key uniquely identifying the object to be removed.
     */
    void remove(Object key);

    /**
     * Remove all of the objects from this cache.
     */
    void removeAll();
}