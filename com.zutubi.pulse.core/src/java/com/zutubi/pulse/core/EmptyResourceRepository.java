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
 * A resource repository with no resources.
 */
public class EmptyResourceRepository implements ResourceRepository
{
    public boolean hasResource(ResourceRequirement requirement)
    {
        return false;
    }

    public boolean satisfies(Iterable<? extends ResourceRequirement> requirements)
    {
        for (ResourceRequirement requirement: requirements)
        {
            if (!requirement.isOptional())
            {
                return false;
            }
        }

        return true;
    }

    public boolean hasResource(String name)
    {
        return false;
    }

    public ResourceConfiguration getResource(String name)
    {
        return null;
    }
}
