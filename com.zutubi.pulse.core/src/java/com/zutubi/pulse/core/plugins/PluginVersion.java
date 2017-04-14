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

import com.zutubi.util.StringUtils;

/**
 */
public class PluginVersion implements Comparable<PluginVersion>
{
    public static final PluginVersion NONE = new PluginVersion(0, 0, 0, null);
    public static final PluginVersion MAX = new PluginVersion(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, null);
    
    private int major;
    private int minor;
    private int service;
    private String qualifier;
    
    public PluginVersion(String version) throws IllegalArgumentException
    {
        if (version == null)
        {
            throw new IllegalArgumentException("Version can not be null.");
        }
        String[] pieces = version.split("\\.", 4);
        if(pieces.length < 3)
        {
            throw new IllegalArgumentException("Version contains less than three segments");
        }

        try
        {
            major = Integer.parseInt(pieces[0]);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid major number '" + pieces[0] + "'");
        }

        try
        {
            minor = Integer.parseInt(pieces[1]);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid minor number '" + pieces[1] + "'");
        }

        try
        {
            service = Integer.parseInt(pieces[2]);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid service number '" + pieces[2] + "'");
        }

        if(pieces.length == 4)
        {
            qualifier = pieces[3];
        }
        else
        {
            qualifier = null;
        }
    }

    public PluginVersion(int major, int minor, int service, String qualifier)
    {
        this.major = major;
        this.minor = minor;
        this.service = service;
        this.qualifier = qualifier;
    }

    public int getMajor()
    {
        return major;
    }

    public int getMinor()
    {
        return minor;
    }

    public int getService()
    {
        return service;
    }

    public String getQualifier()
    {
        return qualifier;
    }

    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof PluginVersion))
        {
            return false;
        }

        PluginVersion other = (PluginVersion) obj;
        return compareTo(other) == 0;
    }

    public int hashCode()
    {
        return toString().hashCode();
    }

    public String toString()
    {
        return String.format("%d.%d.%d", major, minor, service) + (StringUtils.stringSet(qualifier) ? ("." + qualifier) : "");
    }

    public int compareTo(PluginVersion o)
    {
        int result = major - o.major;
        if(result == 0)
        {
            result = minor - o.minor;
            if(result == 0)
            {
                result = service - o.service;
                if(result == 0 && qualifier != null && o.qualifier != null)
                {
                    result = qualifier.compareTo(o.qualifier);
                }
            }
        }

        return result;
    }
}
