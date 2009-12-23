package com.zutubi.pulse.master.scm;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.scm.polling.PollingService;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;

public class DefaultScmManager implements ScmManager
{
    private EventManager eventManager;
    private ScmClientFactory<ScmConfiguration> scmClientFactory;
    private ScmContextFactory scmContextFactory;

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

    public ScmContext createContext(ProjectConfiguration projectConfiguration) throws ScmException
    {
        return scmContextFactory.createContext(projectConfiguration);
    }

    public ScmClient createClient(ScmConfiguration config) throws ScmException
    {
        return scmClientFactory.createClient(config);
    }

    public void setPollingService(PollingService pollingService)
    {
        this.pollingService = pollingService;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setScmClientFactory(ScmClientFactory<ScmConfiguration> scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }

    public void setScmContextFactory(ScmContextFactory scmContextFactory)
    {
        this.scmContextFactory = scmContextFactory;
    }
}
