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

package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;

import java.util.Collection;
import java.util.Set;

/**
 * A cache of configuration instances.  These instances are shared and thus
 * should be treated as read-only.
 */
public interface InstanceCache
{
    void markInvalid(String path);

    boolean isValid(String path, boolean allowIncomplete);

    /**
     * Retrieves all instances at or under the given path (i.e. descendants in the configuration path sense).
     *
     * @param path the path to get instances under
     * @param allowIncomplete if true, instances marked incomplete may be returned, if false, they may not
     * @return all instances under the given path
     */
    Collection<Configuration> getAllDescendants(String path, boolean allowIncomplete);

    /**
     * Retrieves the instance at the given path, if one exists.
     *
     * @param path            path of the instance to retrieve
     * @param allowIncomplete if true, instances marked incomplete may be
     *                        returned, if false, they may not
     * @return the instance at the given path, or null if no such instance is
     *         found
     */
    Configuration get(String path, boolean allowIncomplete);

    void getAllMatchingPathPattern(String path, Collection<Configuration> result, boolean allowIncomplete);

    <T extends Configuration> void getAllMatchingPathPattern(String path, Class<T> clazz, Collection<T> result, boolean allowIncomplete);

    void put(String path, Configuration instance, boolean complete);

    /**
     * This method traverses the instance cache, passing each instance to specified instance
     * handler.
     *
     * @param handler               the callback
     * @param allowIncomplete   allow incomplete instances to be passed to the handler
     * @param writable              if the handler intends to make any changes to the instance, then writable
     * must be true so that the change is persisted.
     */
    void forAllInstances(InstanceHandler handler, boolean allowIncomplete, boolean writable);

    /**
     * Marks an instance as dirty: i.e. the instance or something it reaches
     * has changed and needs refreshing.
     *
     * @param path the path to mark dirty
     * @return if an entry wsa found at the path and was newly-marked as dirty
     */
    boolean markDirty(String path);

    /**
     * Clears all dirty instances from the cache, ready for them to be
     * refreshed.
     */
    void clearDirty();

    /**
     * Gets the set of all paths for instances which make reference to the
     * given path.  The references will be from properties on the instance.
     *
     * @param path the path being referenced
     * @return the set of all referencing instance paths
     *
     * @see #getPropertyPathsReferencing(String)
     */
    Set<String> getInstancePathsReferencing(String path);

    /**
     * Gets the set of all paths for properties which make reference to the
     * given path.  These will be paths to fields, or paths to items of a list
     * field (i.e. ending in a list index).
     *
     * @param path the path being referenced
     * @return the set of all referencing property paths
     *
     * @see #getPropertyPathsReferencing(String)
     */
    Set<String> getPropertyPathsReferencing(String path);

    /**
     * Indexes a reference from a property to an instance.  The property path
     * should be in the same form returned by {@link #getPropertyPathsReferencing(String)}.
     *
     * @param fromPropertyPath path of the property making reference
     * @param toPath           path of the instance being referenced
     */
    void indexReference(String fromPropertyPath, String toPath);

    public static interface InstanceHandler
    {
        void handle(Configuration instance, String baseName, boolean complete, Configuration parentInstance);
    }
}
