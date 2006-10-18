package com.zutubi.pulse.agent;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.MasterBuildService;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.StartupManager;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.logging.ServerMessagesHandler;

import java.util.List;

/**
 */
public class MasterAgent implements Agent
{
    private MasterBuildService service;
    private MasterConfigurationManager configurationManager;
    private StartupManager startupManager;
    private ServerMessagesHandler serverMessagesHandler;

    public MasterAgent(MasterBuildService service, MasterConfigurationManager configurationManager, StartupManager startupManager, ServerMessagesHandler serverMessagesHandler)
    {
        this.service = service;
        this.configurationManager = configurationManager;
        this.startupManager = startupManager;
        this.serverMessagesHandler = serverMessagesHandler;
    }

    public long getId()
    {
        return 0;
    }

    public BuildService getBuildService()
    {
        return service;
    }

    public SystemInfo getSystemInfo()
    {
        return SystemInfo.getSystemInfo(configurationManager, startupManager);
    }

    public List<CustomLogRecord> getRecentMessages()
    {
        return serverMessagesHandler.takeSnapshot();
    }

    public boolean isOnline()
    {
        return isEnabled();
    }

    public boolean isEnabled()
    {
        MasterConfiguration masterConfig = configurationManager.getAppConfig();
        return masterConfig.isMasterEnabled();
    }

    public Status getStatus()
    {
        if (!isEnabled())
        {
            return Status.DISABLED;
        }
        
        if(service.getBuildingRecipe() == 0)
        {
            return Status.IDLE;
        }
        else
        {
            return Status.BUILDING;
        }
    }

    public void setStatus(Status status)
    {
        MasterConfiguration masterConfig = configurationManager.getAppConfig();
        if (status == Status.DISABLED)
        {
            masterConfig.setMasterEnabled(false);
        }
        else
        {
            masterConfig.setMasterEnabled(true);
        }
    }

    public String getLocation()
    {
        SystemConfiguration systemConfig = configurationManager.getSystemConfig();
        return constructMasterLocation(configurationManager.getAppConfig(), systemConfig);
    }

    public boolean isSlave()
    {
        return false;
    }

    public String getName()
    {
        return "master";
    }

    public static String constructMasterLocation(MasterConfiguration appConfig, SystemConfiguration systemConfig)
    {
        String url = appConfig.getAgentHost() + ":" + systemConfig.getServerPort() + systemConfig.getContextPath();
        if(url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }
}
