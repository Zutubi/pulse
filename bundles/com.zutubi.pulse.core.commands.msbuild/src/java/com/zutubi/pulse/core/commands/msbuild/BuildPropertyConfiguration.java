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

package com.zutubi.pulse.core.commands.msbuild;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Configures a named build property to pass to MsBuild.
 */
@SymbolicName("zutubi.msbuildCommandConfig.buildPropertyConfig")
@Form(fieldOrder = {"name", "value"})
public class BuildPropertyConfiguration extends AbstractNamedConfiguration
{
    @Required
    private String value;

    public BuildPropertyConfiguration()
    {
    }

    public BuildPropertyConfiguration(String name, String value)
    {
        setName(name);
        setValue(value);
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
