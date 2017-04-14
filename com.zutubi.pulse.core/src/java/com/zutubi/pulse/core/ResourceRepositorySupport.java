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

/**
 * Support base class for implementing resource repositories.  Implements
 * convenience methods on top of the basic resource access method.
 */
public abstract class ResourceRepositorySupport implements ResourceRepository
{
    public boolean hasResource(ResourceRequirement requirement)
    {
        ResourceConfiguration resource = getResource(requirement.getResource());
        return resource != null && hasRequiredVersion(resource, requirement);
    }

    public boolean satisfies(Iterable<? extends ResourceRequirement> requirements)
    {
        for (ResourceRequirement resourceRequirement: requirements)
        {
            boolean haveResource = hasResource(resourceRequirement);
            if (resourceRequirement.isInverse())
            {
                if (haveResource)
                {
                    return false;
                }
            }
            else if (!haveResource && !resourceRequirement.isOptional())
            {
                return false;
            }
        }

        return true;
    }

    private boolean hasRequiredVersion(ResourceConfiguration resource, ResourceRequirement requirement)
    {
        return requirement.isDefaultVersion() || resource.getVersion(requirement.getVersion()) != null;
    }

    public boolean hasResource(String name)
    {
        return getResource(name) != null;
    }
}
