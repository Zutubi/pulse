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

package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.HostManager;
import com.zutubi.pulse.master.agent.HostService;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.servercore.ServerInfoModel;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.StartupManager;
import com.zutubi.util.logging.Logger;

/**
 * An action to yield JSON data for the server and agent info tabs.
 */
public class ServerInfoDataAction extends AgentActionBase
{
    private static final Logger LOG = Logger.getLogger(ServerInfoDataAction.class);

    private ServerInfoModel info;

    private ConfigurationManager configurationManager;
    private HostManager hostManager;
    private StartupManager startupManager;

    public ServerInfoModel getInfo()
    {
        return info;
    }

    public String execute()
    {
        Agent agent = getAgent();
        boolean includeDetailed = accessManager.hasPermission(ServerPermission.ADMINISTER.name(), null);
        if(agent == null)
        {
            info = ServerInfoModel.getServerInfo(configurationManager, startupManager, includeDetailed);
        }
        else
        {
            if (agent.isOnline())
            {
                HostService hostService = hostManager.getServiceForHost(agent.getHost());
                try
                {
                    info = hostService.getSystemInfo(includeDetailed);
                }
                catch (RuntimeException e)
                {
                    LOG.warning(e);
                    throw new RuntimeException("Unable to contact agent: " + e.getMessage(), e);
                }
            }
            else
            {
                throw new RuntimeException("Agent is not online.");
            }
        }
        
        return SUCCESS;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setHostManager(HostManager hostManager)
    {
        this.hostManager = hostManager;
    }

    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }
}
