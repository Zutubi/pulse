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

package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.annotations.Required;

/**
 * A basic resource requirement configuration used when loading requirements
 * from a resources file (e.g. local builds).
 */
@SymbolicName("zutubi.simpleResourceRequirementConfig")
public class SimpleResourceRequirementConfiguration extends AbstractConfiguration
{
    @Required
    private String name;
    private String version;
    private boolean inverse = false;
    private boolean optional = false;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public boolean isInverse()
    {
        return inverse;
    }

    public void setInverse(boolean inverse)
    {
        this.inverse = inverse;
    }

    public boolean isOptional()
    {
        return optional;
    }

    public void setOptional(boolean optional)
    {
        this.optional = optional;
    }

    public ResourceRequirement asResourceRequirement()
    {
        if (StringUtils.stringSet(version))
        {
            return new ResourceRequirement(name, version, inverse, optional);
        }
        else
        {
            return new ResourceRequirement(name, inverse, optional);
        }
    }
}
