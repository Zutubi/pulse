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
import com.zutubi.pulse.model.Slave;

import java.util.List;

/**
 */
public class MasterAgent implements Agent
{
    private Status status;
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

        if(getEnableState() == Slave.EnableState.ENABLED)
        {
            status = Status.IDLE;
        }
        else
        {
            status = Status.DISABLED;
        }
    }

    public long getId()
    {
        return 0;
    }

    public BuildService getBuildService()
    {
        return service;
    }

    public long getRecipeId()
    {
        return service.getBuildingRecipe();
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
        return getStatus().isOnline();
    }

    public Slave.EnableState getEnableState()
    {
        MasterConfiguration masterConfig = configurationManager.getAppConfig();
        return masterConfig.getMasterEnableState();
    }

    public boolean isEnabled()
    {
        return getEnableState() == Slave.EnableState.ENABLED;
    }

    public Status getStatus()
    {
        return status;
    }

    public void updateStatus(Status status)
    {
        this.status = status;
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

    public boolean isDisabling()
    {
        return getEnableState() == Slave.EnableState.DISABLING;
    }

    public String getName()
    {
        return "master";
    }

    private String constructMasterLocation(MasterConfiguration appConfig, SystemConfiguration systemConfig)
    {
        DefaultMasterLocationProvider provider = new DefaultMasterLocationProvider();
        provider.setMasterConfiguration(appConfig);
        provider.setSystemConfiguration(systemConfig);
        return provider.getMasterLocation();
    }
}
