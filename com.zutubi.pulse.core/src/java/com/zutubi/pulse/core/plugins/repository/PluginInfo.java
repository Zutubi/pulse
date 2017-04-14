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

package com.zutubi.pulse.core.plugins.repository;

/**
 * Contains details of a plugin from a repository.
 */
public class PluginInfo
{
    private String id;
    private String version;
    private PluginRepository.Scope scope;

    public PluginInfo(String id, String version, PluginRepository.Scope scope)
    {
        this.id = id;
        this.version = version;
        this.scope = scope;
    }

    /**
     * The plugin identifier, by convention in Java package notation (e.g.
     * com.zutubi.pulse.core.scm.svn).
     * 
     * @return a unique identifier for this plugin
     */
    public String getId()
    {
        return id;
    }

    /**
     * The version of the plugin.
     * 
     * @return the plugin version
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Indicates which scope the plugin belongs to.
     * 
     * @return the plugin's scope
     */
    public PluginRepository.Scope getScope()
    {
        return scope;
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

        PluginInfo that = (PluginInfo) o;

        if (scope != that.scope)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
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
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return id + ":" + version + " (" + scope + ")";
    }
}
