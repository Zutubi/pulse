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

import com.zutubi.pulse.core.resources.api.ResourceConfiguration;

import java.util.*;

/**
 * A repository that holds a cache of resource information in memory.
 */
public class InMemoryResourceRepository extends ResourceRepositorySupport
{
    private Map<String, ResourceConfiguration> resources = new TreeMap<String, ResourceConfiguration>();

    public ResourceConfiguration getResource(String name)
    {
        return resources.get(name);
    }

    /**
     * Add the given resource to this repository.  If a resource of the same
     * name already exists, this one replaces it.
     *
     * @param resource the resource to add
     */
    public void addResource(ResourceConfiguration resource)
    {
        resources.put(resource.getName(), resource);
    }

    /**
     * Adds all of the given resources to this repoistory via {@link #addResource(com.zutubi.pulse.core.resources.api.ResourceConfiguration)}.
     *
     * @param resources collection of resources to add
     */
    public void addAllResources(Collection<? extends ResourceConfiguration> resources)
    {
        for (ResourceConfiguration r: resources)
        {
            addResource(r);
        }
    }

    /**
     * Returns the names of all resources in this repository.
     *
     * @return names for all if the resources in this repository
     */
    public List<String> getResourceNames()
    {
        return new LinkedList<String>(resources.keySet());
    }
}
