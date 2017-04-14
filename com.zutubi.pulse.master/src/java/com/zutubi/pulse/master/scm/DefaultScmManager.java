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

package com.zutubi.pulse.master.scm;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.polling.PollingService;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;

public class DefaultScmManager implements ScmManager
{
    private EventManager eventManager;
    private MasterScmClientFactory scmClientFactory;
    private MasterScmContextFactory scmContextFactory;

    private PollingService pollingService;

    public void init()
    {
        eventManager.register(new SystemStartedListener()
        {
            public void systemStarted()
            {
                initialise();
            }
        });
    }

    private void initialise()
    {
        pollingService.init();
    }

    public void clearCache(long projectId)
    {
        pollingService.clearCache(projectId);
    }

    public ScmContext createContext(ScmConfiguration scmConfiguration, String implicitResource)
    {
        return scmContextFactory.createContext(scmConfiguration, implicitResource);
    }

    public ScmContext createContext(String implicitResource)
    {
        return scmContextFactory.createContext(implicitResource);
    }

    public ScmContext createContext(ProjectConfiguration projectConfiguration, Project.State projectState, String implicitResource) throws ScmException
    {
        return scmContextFactory.createContext(projectConfiguration, projectState, implicitResource);
    }

    public ScmClient createClient(ProjectConfiguration project, ScmConfiguration config) throws ScmException
    {
        return scmClientFactory.createClient(project, config);
    }

    public void setPollingService(PollingService pollingService)
    {
        this.pollingService = pollingService;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setScmClientFactory(MasterScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }

    public void setScmContextFactory(MasterScmContextFactory scmContextFactory)
    {
        this.scmContextFactory = scmContextFactory;
    }
}
