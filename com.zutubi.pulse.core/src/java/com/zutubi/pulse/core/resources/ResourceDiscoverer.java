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

package com.zutubi.pulse.core.resources;

import com.zutubi.pulse.core.InMemoryResourceRepository;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceLocator;

import java.util.List;

/**
 * The resource discoverer is responsible for the automatic discovery of common resources.
 *
 * The configuration of the resources along with the smarts about the various resources is delegated to
 * the ResourceConstructor implementations.
 * 
 */
public class ResourceDiscoverer
{
    private ResourceLocator locator;

    /**
     * Create a new discoverer that finds resource using the given locator.
     * 
     * @param locator locator used to discover resources
     */
    public ResourceDiscoverer(ResourceLocator locator)
    {
        this.locator = locator;
    }

    /**
     * Run the resource discovery process, returning a list of all the discovered resources.
     *
     * @return list of discovered resources.
     */
    public List<ResourceConfiguration> discover()
    {
        return locator.locate();
    }

    public void discoverAndAdd(InMemoryResourceRepository repository)
    {
        List<ResourceConfiguration> resources = discover();
        for(ResourceConfiguration r: resources)
        {
            if(!repository.hasResource(r.getName()))
            {
                repository.addResource(r);
            }
        }
    }
}
