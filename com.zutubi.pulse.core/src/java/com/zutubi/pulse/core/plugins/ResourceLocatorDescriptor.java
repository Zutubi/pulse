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

package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceLocator;

import java.util.LinkedList;
import java.util.List;

/**
 * Describes a plugged-in resource locator.
 */
public class ResourceLocatorDescriptor
{
    private String name;
    private Class<? extends ResourceLocator> clazz;
    
    public ResourceLocatorDescriptor(String name, Class<? extends ResourceLocator> clazz)
    {
        this.name = name;
        this.clazz = clazz;
    }

    public String getName()
    {
        return name;
    }

    public Class<? extends ResourceLocator> getClazz()
    {
        return clazz;
    }
}
