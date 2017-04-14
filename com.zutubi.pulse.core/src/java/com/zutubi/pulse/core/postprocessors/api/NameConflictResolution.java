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

package com.zutubi.pulse.core.postprocessors.api;

/**
 * Available policies for resolving name conflicts.
 */
public enum NameConflictResolution
{
    /**
     * Combine entities with the same name by taking the worst result.
     */
    WORST_RESULT(false),

    /**
     * Combine entities with the same name by taking the best result.
     */
    BEST_RESULT(false),

    /**
     * Combine entities with the same name by taking the first result seen.
     */
    FIRST_RESULT(false),

    /**
     * Combine entities with the same name by taking the last result seen.
     */
    LAST_RESULT(false),

    /**
     * Resolve the conflict by appending an increasing integer to the name
     * until a unique name is found.
     */
    APPEND(true),

    /**
     * Resolve the conflict by prepending an increasing integer to the name
     * until a unique name is found.
     */
    PREPEND(true);

    private boolean uniqueNameGenerated;

    NameConflictResolution(boolean uniqueNameGenerated)
    {
        this.uniqueNameGenerated = uniqueNameGenerated;
    }

    /**
     * @return true iff this type of resolution works by given conflicting cases unique names
     */
    public boolean isUniqueNameGenerated()
    {
        return uniqueNameGenerated;
    }
}
