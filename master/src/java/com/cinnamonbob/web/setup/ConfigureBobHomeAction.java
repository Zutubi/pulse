package com.cinnamonbob.web.setup;

import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.bootstrap.SimpleConfigurationManager;
import com.cinnamonbob.bootstrap.StartupManager;
import com.cinnamonbob.web.ActionSupport;

import java.io.File;

/**
 * <class-comment/>
 */
public class ConfigureBobHomeAction extends ActionSupport
{
    private ConfigurationManager configurationManager;
    private StartupManager startupManager;

    private String bobHome;

    public String getBobHome()
    {
        return bobHome;
    }

    public void setBobHome(String bobHome)
    {
        this.bobHome = bobHome;
    }

    public String doInput() throws Exception
    {
        // set the default.
        String install = System.getProperty(SimpleConfigurationManager.BOB_INSTALL);
        if (install == null)
        {
            this.bobHome = "home";
        }
        else
        {
            this.bobHome = install + File.separatorChar + "home";
        }

        // make the path the shortest possible.
        this.bobHome = new File(this.bobHome).getCanonicalPath();

        return INPUT;
    }

    public String execute()
    {
        configurationManager.setBobHome(new File(bobHome));

        // startup manager . continue..
        startupManager.startApplication();

        return SUCCESS;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }
}
