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

package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.servercore.ServerInfoModel;
import com.zutubi.pulse.servercore.services.HostStatus;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.pulse.servercore.services.SlaveService;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;

import java.util.List;

/**
 * Implementation of the service used to communicate with remote hosts.
 */
public class SlaveHostService implements HostService
{
    private SlaveService service;
    private ServiceTokenManager serviceTokenManager;

    public SlaveHostService(SlaveService slaveService)
    {
        this.service = slaveService;
    }

    public int ping()
    {
        return service.ping();
    }

    public HostStatus getStatus(String masterLocation)
    {
        return service.getStatus(serviceTokenManager.getToken(), masterLocation);
    }

    public ServerInfoModel getSystemInfo(boolean includeDetailed)
    {
        return service.getSystemInfo(serviceTokenManager.getToken(), includeDetailed);
    }

    public List<CustomLogRecord> getRecentMessages()
    {
        return service.getRecentMessages(serviceTokenManager.getToken());
    }

    public void garbageCollect()
    {
        service.garbageCollect();
    }

    public List<ResourceConfiguration> discoverResources()
    {
        return service.discoverResources(serviceTokenManager.getToken());
    }

    public boolean updateVersion(String masterBuild, String masterUrl, long hostId, String packageUrl, long packageSize)
    {
        return service.updateVersion(serviceTokenManager.getToken(), masterBuild, masterUrl, hostId, packageUrl, packageSize);
    }

    public boolean syncPlugins(String masterUrl, long hostId, String pluginRepositoryUrl)
    {
        return service.syncPlugins(serviceTokenManager.getToken(), masterUrl, hostId, pluginRepositoryUrl);
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }
}
