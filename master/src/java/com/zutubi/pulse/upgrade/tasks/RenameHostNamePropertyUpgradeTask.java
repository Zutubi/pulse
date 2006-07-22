package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.conf.ConfigSupport;
import com.zutubi.pulse.upgrade.ConfigurationAware;
import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.upgrade.UpgradeTask;

import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class RenameHostNamePropertyUpgradeTask implements UpgradeTask, ConfigurationAware
{
    protected int buildNumber;

    private List<String> errors = new LinkedList<String>();

    private MasterConfigurationManager configurationManager;

    public String getName()
    {
        return "Rename hostname property.";
    }

    public String getDescription()
    {
        return "Rename and upgrade the hostname property to the new base url property.";
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public void execute(UpgradeContext context) throws UpgradeException
    {
        ConfigSupport appConfig = (ConfigSupport) configurationManager.getAppConfig();
        if (appConfig.hasProperty("host.name"))
        {
            String hostname = appConfig.getProperty("host.name");
            appConfig.removeProperty("host.name");
            String baseUrl = "http://" + hostname;
            if (baseUrl.endsWith("/"))
            {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            appConfig.setProperty("webapp.base.url", baseUrl);
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public boolean hasFailed()
    {
        return false;
    }
}
