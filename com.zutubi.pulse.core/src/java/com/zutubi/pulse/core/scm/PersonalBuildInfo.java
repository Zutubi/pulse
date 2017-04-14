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

package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.plugins.repository.PluginInfo;

import java.util.List;

/**
 * Used to transfer information required for a personal build from the Pulse
 * master to the personal build client.  The client uses this information to
 * check its own state, and that the working copy it is looking at matches the
 * project on the master.
 */
public class PersonalBuildInfo
{
    public static final String PLUGINS = "plugins";
    public static final String SCM_TYPE = "scmType";
    public static final String SCM_LOCATION = "scmLocation";
    
    /**
     * Indicates the type of SCM the project uses.  Represented as a string
     * to allow plugin implementations to define their own types easily.
     */
    private String scmType;
    /**
     * The location of the project in the SCM according to the master
     * configuration.
     */
    private String scmLocation;
    /**
     * A list of CORE-scoped plugins running on the master, so we can check if
     * we need to sync.
     */
    private List<PluginInfo> plugins;

    public PersonalBuildInfo(String scmType, String scmLocation, List<PluginInfo> plugins)
    {
        this.scmType = scmType;
        this.scmLocation = scmLocation;
        this.plugins = plugins;
    }

    public String getScmType()
    {
        return scmType;
    }

    public String getScmLocation()
    {
        return scmLocation;
    }

    public List<PluginInfo> getPlugins()
    {
        return plugins;
    }
}
