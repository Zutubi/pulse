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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Configuration to set a single custom field in a {@link CustomFieldsCommand}.
 */
@SymbolicName("zutubi.customFieldConfig")
@Form(fieldOrder = {"name", "value", "scope"})
public class CustomFieldConfiguration extends AbstractNamedConfiguration
{
    private String value;
    @Required
    private FieldScope scope = FieldScope.RECIPE;

    public CustomFieldConfiguration()
    {
    }

    public CustomFieldConfiguration(String name, String value, FieldScope scope)
    {
        super(name);
        this.value = value;
        this.scope = scope;
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

    public FieldScope getScope()
    {
        return scope;
    }

    public void setScope(FieldScope scope)
    {
        this.scope = scope;
    }
}