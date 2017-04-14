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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.FieldAction;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Transient type allowing the user to select/configure the addition of a default resource
 * requirement when adding a command.
 */
@SymbolicName("zutubi.commandResourceConfig")
@Form(fieldOrder = {"addDefaultResource", "resource", "defaultVersion", "version", "optional"})
public class CommandResourceConfiguration extends AbstractConfiguration
{
    // NOTE: leaving this false is deliberate: we init it to true client side if the user sees this
    // step.  This way we only see it posted back as true when this step applies.
    @ControllingCheckbox
    private boolean addDefaultResource;
    @Required
    @FieldAction(template = "CommandResourceConfiguration.resource")
    private String resource;
    @ControllingCheckbox(uncheckedFields = {"version"})
    private boolean defaultVersion = true;
    @Required // if version is enabled, it is also required.
    private String version;
    private boolean optional = true;

    public boolean isAddDefaultResource()
    {
        return addDefaultResource;
    }

    public void setAddDefaultResource(boolean addDefaultResource)
    {
        this.addDefaultResource = addDefaultResource;
    }

    public String getResource()
    {
        return resource;
    }

    public void setResource(String resource)
    {
        this.resource = resource;
    }

    public boolean isDefaultVersion()
    {
        return defaultVersion;
    }

    public void setDefaultVersion(boolean defaultVersion)
    {
        this.defaultVersion = defaultVersion;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public boolean isOptional()
    {
        return optional;
    }

    public void setOptional(boolean optional)
    {
        this.optional = optional;
    }

    public ResourceRequirementConfiguration buildRequirement()
    {
        return new ResourceRequirementConfiguration(resource, version, defaultVersion, optional);
    }
}
