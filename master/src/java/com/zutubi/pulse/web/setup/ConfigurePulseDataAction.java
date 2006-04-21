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
public class ConfigurePulseDataAction extends ActionSupport
{
    private ConfigurationManager configurationManager;

    private SetupManager setupManager;

    private String data;

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public void validate()
    {
        // attempt to create the data directory. If this fails, we need to ask the
        // user for another directory.
        File data = new File(this.data);
        if (!data.exists() && !data.mkdirs())
        {
            addFieldError("data", getText(""));
        }
    }

    public String doInput() throws Exception
    {
        // set the default.
        String data = System.getProperty(SimpleConfigurationManager.PULSE_HOME);
        if (data == null)
        {
            this.data = "data";
        }
        else
        {
            this.data = data + File.separatorChar + "data";
        }

        // make the path the shortest possible.
        this.data = new File(this.data).getCanonicalPath();

        return INPUT;
    }

    public String execute()
    {
        File home = new File(this.data);
        configurationManager.setPulseData(home);

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
