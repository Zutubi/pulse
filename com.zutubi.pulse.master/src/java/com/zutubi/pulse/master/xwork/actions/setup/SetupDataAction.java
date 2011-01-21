package com.zutubi.pulse.master.xwork.actions.setup;

import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.tove.config.setup.SetupDataConfiguration;
import com.zutubi.pulse.master.tove.webwork.TransientAction;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.StringUtils;

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
        super("init/data");
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
        String userHome = System.getProperty(EnvConfig.USER_HOME);
        if (StringUtils.stringSet(userHome))
        {
            String userConfig = configurationManager.getEnvConfig().getDefaultPulseConfigDir(MasterConfigurationManager.CONFIG_DIR);
            data = FileSystemUtils.composeFilename(userConfig, "data");
        }
        else
        {
            data = "data";
        }

        config.setData(FileSystemUtils.normaliseSeparators(new File(data).getCanonicalPath()));
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
