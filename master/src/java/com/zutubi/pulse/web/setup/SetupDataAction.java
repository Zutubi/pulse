package com.zutubi.pulse.web.setup;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SimpleMasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.bootstrap.Data;

import java.io.File;

/**
 * <class-comment/>
 */
public class SetupDataAction extends SetupActionSupport
{
    private MasterConfigurationManager configurationManager;
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
            addFieldError("data", getText("Failed to create the specified data directory."));
        }
    }

    public String doInput() throws Exception
    {
        // set the default.
        String data = System.getProperty(SimpleMasterConfigurationManager.PULSE_HOME);
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

    public String execute() throws Exception
    {
        File home = new File(this.data);
        configurationManager.setPulseData(home);

        // If this is the first time this directory is being used as a data directory, then we need
        // to ensure that it is initialised. If we are working with an already existing directory,
        // then it will have been initialised and no re-initialisation is required (or allowed).
        Data d = configurationManager.getData();
        if (!d.isInitialised())
        {
            configurationManager.getData().init();
        }

        setupManager.requestDataComplete();
        
        return SUCCESS;
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    /**
     * Required resource.
     *
     * @param setupManager
     */
    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
