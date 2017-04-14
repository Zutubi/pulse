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

package com.zutubi.pulse.core.engine.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * A simple string-valued reference.
 */
@SymbolicName("zutubi.property")
@Referenceable(valueProperty = "value")
public class PropertyConfiguration extends AbstractNamedConfiguration
{
    private String value;

    public PropertyConfiguration()
    {
    }

    public PropertyConfiguration(String name, String value)
    {
        setName(name);
        this.value = value;
    }

    @Override @Required
    public String getName()
    {
        return super.getName();
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
