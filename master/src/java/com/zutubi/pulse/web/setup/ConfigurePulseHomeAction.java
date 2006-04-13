/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.setup;

import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.bootstrap.SimpleConfigurationManager;
import com.zutubi.pulse.web.ActionSupport;

import java.io.File;

/**
 * <class-comment/>
 */
public class ConfigurePulseHomeAction extends ActionSupport
{
    private ConfigurationManager configurationManager;

    private SetupManager setupManager;

    private String pulseHome;

    public String getPulseHome()
    {
        return pulseHome;
    }

    public void setPulseHome(String pulseHome)
    {
        this.pulseHome = pulseHome;
    }

    public void validate()
    {
        // attempt to create the home directory. If this fails, we need to ask the
        // user for another directory.
        File home = new File(pulseHome);
        if (!home.exists() && !home.mkdirs())
        {
            addFieldError("pulseHome", getText(""));
        }
    }

    public String doInput() throws Exception
    {
        // set the default.
        String install = System.getProperty(SimpleConfigurationManager.PULSE_INSTALL);
        if (install == null)
        {
            this.pulseHome = "home";
        }
        else
        {
            this.pulseHome = install + File.separatorChar + "home";
        }

        // make the path the shortest possible.
        this.pulseHome = new File(this.pulseHome).getCanonicalPath();

        return INPUT;
    }

    public String execute()
    {
        File home = new File(pulseHome);
        configurationManager.setPulseHome(home);

        // next we need to know if we need to upgrade or setup.
        if (setupManager.systemRequiresSetup())
        {
            // need to setup the database
            setupManager.prepareSetup();

            return "setup";
        }
        else if (setupManager.systemRequiresUpgrade())
        {
            return "upgrade";
        }
        else
        {
            // this may take a little while, need to provide some user feedback.
            setupManager.setupComplete();
            return SUCCESS;
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
