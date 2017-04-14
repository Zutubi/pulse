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

import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.annotations.TextArea;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

@Form(fieldOrder = { "name", "value", "description", "addToEnvironment", "addToPath" })
@Table(columns = {"name", "value"})
@SymbolicName("zutubi.resourceProperty")
public class ResourcePropertyConfiguration extends AbstractNamedConfiguration
{
    @TextArea(autoSize = true)
    private String value;
    private boolean addToEnvironment = false;
    private boolean addToPath = false;
    private String description;

    public ResourcePropertyConfiguration()
    {
    }

    public ResourcePropertyConfiguration(String name, String value)
    {
        this(name, value, false, false);
    }

    public ResourcePropertyConfiguration(String name, String value, boolean addToEnvironment, boolean addToPath)
    {
        super(name);
        this.value = value;
        this.addToEnvironment = addToEnvironment;
        this.addToPath = addToPath;
    }

    public ResourcePropertyConfiguration(ResourceProperty p)
    {
        this(p.getName(), p.getValue(), p.getAddToEnvironment(), p.getAddToPath());
    }

    public ResourcePropertyConfiguration copy()
    {
        return new ResourcePropertyConfiguration(getName(), value, addToEnvironment, addToPath);
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public boolean getAddToEnvironment()
    {
        return addToEnvironment;
    }

    public void setAddToEnvironment(boolean addToEnvironment)
    {
        this.addToEnvironment = addToEnvironment;
    }

    public boolean getAddToPath()
    {
        return addToPath;
    }

    public void setAddToPath(boolean addToPath)
    {
        this.addToPath = addToPath;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public ResourceProperty asResourceProperty()
    {
        return new ResourceProperty(getName(), getValue(), getAddToEnvironment(), getAddToPath());
    }
}