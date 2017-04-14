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

package com.zutubi.pulse.slave.api;

import com.google.common.collect.Iterables;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.PluginRunningPredicate;
import com.zutubi.pulse.core.plugins.repository.PluginList;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.pulse.servercore.api.AdminTokenManager;
import com.zutubi.pulse.servercore.api.AuthenticationException;

import java.util.Hashtable;
import java.util.Vector;

/**
 */
public class RemoteApi
{
    private AdminTokenManager tokenManager;
    private ShutdownManager shutdownManager;
    private PluginManager pluginManager;
    
    public RemoteApi()
    {
    }

    public boolean shutdown(String token, boolean force, boolean exitJvm) throws AuthenticationException
    {
        // Sigh ... this is tricky, because if we shutdown here Jetty dies
        // before this request is complete and the client gets an error :-|.
        checkToken(token);
        shutdownManager.delayedShutdown(force, exitJvm);
        return true;
    }

    public boolean stopService(String token) throws AuthenticationException
    {
        checkToken(token);
        shutdownManager.delayedStop();
        return true;
    }

    public int getBuildNumber(String token) throws AuthenticationException
    {
        checkToken(token);
        return Version.getVersion().getBuildNumberAsInt();
    }

    public Vector<Hashtable<String, Object>> getRunningPlugins(String token)
    {
        checkToken(token);
        Iterable<Plugin> plugins = Iterables.filter(pluginManager.getPlugins(), new PluginRunningPredicate());
        return new Vector<Hashtable<String, Object>>(PluginList.pluginsToHashes(plugins));
    }
    
    private void checkToken(String token) throws AuthenticationException
    {
        if(!tokenManager.checkAdminToken(token))
        {
            throw new AuthenticationException("Invalid token");
        }
    }

    /**
     * A trivial ping method that can be useful for testing connectivity.
     *
     * @return the value "pong"
     */
    public String ping()
    {
        return "pong";
    }

    public void setTokenManager(AdminTokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }

    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
