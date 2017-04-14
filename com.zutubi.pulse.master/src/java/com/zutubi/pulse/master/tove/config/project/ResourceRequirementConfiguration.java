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

import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.variables.VariableResolver;
import com.zutubi.tove.variables.api.VariableMap;
import com.zutubi.validation.annotations.Required;

@Table(columns = {"resource", "displayVersion", "inverse", "optional"})
@Form(fieldOrder = {"resource", "defaultVersion", "version", "inverse", "optional"})
@SymbolicName("zutubi.resourceRequirementConfig")
public class ResourceRequirementConfiguration extends AbstractConfiguration
{
    @Required
    @FieldAction(template = "ResourceRequirementConfiguration.browser")
    private String resource;
    @ControllingCheckbox(uncheckedFields = {"version"})
    private boolean defaultVersion = true;
    @Required // if version is enabled, it is also required.
    private String version;
    @ControllingCheckbox(uncheckedFields = {"optional"})
    private boolean inverse;
    private boolean optional;

    public ResourceRequirementConfiguration()
    {
    }

    public ResourceRequirementConfiguration(String resource, String version, boolean defaultVersion, boolean optional)
    {
        this.resource = resource;
        this.version = version;
        this.defaultVersion = defaultVersion;
        this.optional = optional;
    }

    public ResourceRequirementConfiguration(ResourceRequirement requirement)
    {
        this(requirement.getResource(), requirement.getVersion(), requirement.isDefaultVersion(), requirement.isOptional());
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

    @Transient
    public String getDisplayVersion()
    {
        if (isDefaultVersion())
        {
            return "[default]";
        }
        return getVersion();
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

    public ResourceRequirementConfiguration copy()
    {
        return new ResourceRequirementConfiguration(resource, version, defaultVersion, optional);
    }

    public String toString()
    {
        return (resource == null ? "?" : resource) + ":" + (!defaultVersion ? version : "[default]");
    }

    /**
     * Converts this configuration into a usable requirement, resolving any variable references.
     * 
     * @param variables variables used to resolve references in the resource name or version
     * @return a requirement corresponding to this configuration
     */
    public ResourceRequirement asResourceRequirement(VariableMap variables)
    {
        if (isDefaultVersion())
        {
            return new ResourceRequirement(VariableResolver.safeResolveVariables(resource, variables), inverse, optional);
        }
        else
        {
            return new ResourceRequirement(VariableResolver.safeResolveVariables(resource, variables),
                    VariableResolver.safeResolveVariables(version, variables), inverse, optional);
        }
    }
}

