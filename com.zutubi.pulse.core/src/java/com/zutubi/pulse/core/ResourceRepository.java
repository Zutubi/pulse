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

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;

import java.util.Collection;

/**
 * Base interface for access to resources.  When implementing consider
 * extending {@link com.zutubi.pulse.core.ResourceRepositorySupport}.
 */
public interface ResourceRepository
{
    /**
     * Indicates if the repository has a resource that fulfills the given
     * requirement.
     *
     * @param requirement the requirement to test for, includes the name and
     *                    optionally version of the resource that is required
     * @return true if this repository can fulfil the given requirement
     */
    boolean hasResource(ResourceRequirement requirement);

    /**
     * Indicates if the repository has resources to fulfil all of the given
     * requirements.  Optional requirements are ignored.
     *
     * @param requirements the requirements to test for
     * @return true of this repository can fulfil all of the given requirements
     */
    boolean satisfies(Iterable<? extends ResourceRequirement> requirements);

    /**
     * Indicates if this repository has a resource of the given name.
     *
     * @param name resource name to test for
     * @return true if this repository has a resource of the given name
     */
    boolean hasResource(String name);

    /**
     * Returns the resource with the given name, if one is found in this
     * repository.
     *
     * @param name name of the resource to retrieve
     * @return the resource of the given name, or null if there is no such
     *         resource
     */
    ResourceConfiguration getResource(String name);
}
