package com.zutubi.pulse.web.setup;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class SetupDataAction extends SetupActionSupport
{
    private MasterConfigurationManager configurationManager;
    private SetupManager setupManager;

    private String data;
    private File pulseConfig;

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public String getPulseConfigPath()
    {
        return getPulseConfig().getAbsolutePath();
    }

    public boolean getPulseConfigExists()
    {
        return getPulseConfig().isFile();
    }

    public void validate()
    {
        // if we already have validation errors, then do not continue.
        if (hasErrors())
        {
            return;
        }

        // attempt to create the data directory. If this fails, we need to ask the
        // user for another directory.
        File data = new File(this.data);
        if (!data.exists() && !data.mkdirs())
        {
            addFieldError("data", "Failed to create the specified data directory.");
        }

        // ensure that we have write access to the data directory.
        checkDirectoryIsWritable(data);
    }

    private void checkDirectoryIsWritable(File data)
    {
        File tmpFile = null;
        try
        {
            tmpFile = File.createTempFile("test", "tmp", data);
        }
        catch (IOException e)
        {
            addFieldError("data", "Failed to write to the selected data directory. Please ensure that pulse has " +
                    "permission to write to the specified data directory.");
        }
        finally
        {
            if (tmpFile != null && tmpFile.isFile())
            {
                tmpFile.delete();
            }
        }
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

    public String doInput() throws Exception
    {
        // set the default.
        String userHome = System.getProperty("user.home");
        if (TextUtils.stringSet(userHome))
        {
            String userConfig = configurationManager.getEnvConfig().getDefaultPulseConfigDir(MasterConfigurationManager.CONFIG_DIR);
            this.data = FileSystemUtils.composeFilename(userConfig, "data");
        }
        else
        {
            this.data = "data";
        }

        // make the path the shortest possible.
        this.data = new File(this.data).getCanonicalPath();

        return INPUT;
    }

    public String execute() throws Exception
    {
        File home = new File(this.data);
        configurationManager.setPulseData(home);
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
