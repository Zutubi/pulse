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

package com.zutubi.pulse.core.resources;

import com.zutubi.util.StringUtils;

/**
 * Identifies a required resource for a build stage.
 */
public class ResourceRequirement
{
    private String resource;
    private String version;
    private boolean inverse;
    private boolean optional;

    public ResourceRequirement(String resource, boolean inverse, boolean optional)
    {
        this(resource, null, inverse, optional);
    }

    public ResourceRequirement(String resource, String version, boolean inverse, boolean optional)
    {
        this.resource = resource;
        this.version = version;
        this.inverse = inverse;
        this.optional = optional;
    }

    public String getResource()
    {
        return resource;
    }

    public boolean isInverse()
    {
        return inverse;
    }

    public boolean isOptional()
    {
        return optional;
    }
    
    public boolean isDefaultVersion()
    {
        return !StringUtils.stringSet(version);
    }

    public String getVersion()
    {
        return version;
    }

    public ResourceRequirement copy()
    {
        return new ResourceRequirement(resource, version, inverse, optional);
    }

    public String toString()
    {
        return (inverse ? "!" : "") + (resource == null ? "?" : resource) + ":" + (!isDefaultVersion() ? version : "[default]");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ResourceRequirement that = (ResourceRequirement) o;

        if (inverse != that.inverse)
        {
            return false;
        }
        if (optional != that.optional)
        {
            return false;
        }
        if (resource != null ? !resource.equals(that.resource) : that.resource != null)
        {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = resource != null ? resource.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (inverse ? 1 : 0);
        result = 31 * result + (optional ? 1 : 0);
        return result;
    }
}
