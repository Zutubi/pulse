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

package com.zutubi.pulse.core.resources.api;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.ID;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

@Form(fieldOrder = {"value"})
@SymbolicName("zutubi.resourceVersion")
public class ResourceVersionConfiguration extends AbstractConfiguration
{
    @ID
    private String value;
    @Ordered @Addable("property")
    private Map<String, ResourcePropertyConfiguration> properties = new LinkedHashMap<String, ResourcePropertyConfiguration>();

    public ResourceVersionConfiguration()
    {

    }

    public ResourceVersionConfiguration(String value)
    {
        this.value = value;
    }

    public ResourceVersionConfiguration(ResourceVersionConfiguration v)
    {
        this.value = v.getValue();

        // properties
        for (ResourcePropertyConfiguration rp : v.getProperties().values())
        {
            ResourcePropertyConfiguration rpc = rp.copy();
            addProperty(rpc);
        }
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public Map<String, ResourcePropertyConfiguration> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, ResourcePropertyConfiguration> properties)
    {
        this.properties = properties;
    }

    public boolean hasProperty(String name)
    {
        return properties.containsKey(name);
    }

    public ResourcePropertyConfiguration getProperty(String name)
    {
        return properties.get(name);
    }

    public void addProperty(ResourcePropertyConfiguration p)
    {
        String name = p.getName();
        if (hasProperty(name))
        {
            throw new IllegalArgumentException("Property with name '" + name + "' already exists with value '" + properties.get(name).getValue() + "'");
        }
        properties.put(name, p);
    }

    public void deleteProperty(String name)
    {
        properties.remove(name);
    }
}


