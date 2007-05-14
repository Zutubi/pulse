package com.zutubi.pulse.web.setup;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.webwork.TransientAction;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.prototype.config.setup.SetupDataConfiguration;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;

/**
 */
public class SetupDataAction extends TransientAction<SetupDataConfiguration>
{
    private MasterConfigurationManager configurationManager;
    private SetupManager setupManager;

    private File pulseConfig;

    public SetupDataAction()
    {
        super("setup/data");
    }

    public String getPulseConfigPath()
    {
        return getPulseConfig().getAbsolutePath();
    }

    public boolean getPulseConfigExists()
    {
        return getPulseConfig().isFile();
    }

    public File getPulseConfig()
    {
        if (pulseConfig == null)
        {
            EnvConfig envConfig = configurationManager.getEnvConfig();
            if(envConfig.hasPulseConfig())
            {
                pulseConfig = new File(envConfig.getPulseConfig());
            }
            else
            {
                pulseConfig = new File(envConfig.getDefaultPulseConfig(MasterConfigurationManager.CONFIG_DIR));
            }
        }
        return pulseConfig;
    }

    protected SetupDataConfiguration initialise() throws Exception
    {
        String data;
        SetupDataConfiguration config = new SetupDataConfiguration();
        String userHome = System.getProperty("user.home");
        if (TextUtils.stringSet(userHome))
        {
            String userConfig = configurationManager.getEnvConfig().getDefaultPulseConfigDir(MasterConfigurationManager.CONFIG_DIR);
            data = FileSystemUtils.composeFilename(userConfig, "data");
        }
        else
        {
            data = "data";
        }

        // make the path the shortest possible.
        config.setData(new File(data).getCanonicalPath());
        return config;
    }

    protected String complete(SetupDataConfiguration instance) throws Exception
    {
        File home = new File(instance.getData());
        configurationManager.setPulseData(home);
        setupManager.requestDataComplete();
        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
