package com.zutubi.pulse.web.setup;

import com.zutubi.pulse.web.vfs.FileDialogAction;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;

import java.io.File;

/**
 * <class comment/>
 */
public class PulseDataFileDialogAction extends FileDialogAction
{
    private MasterConfigurationManager configurationManager;

    /**
     * The pulse home directory.
     */
    private String pulseHome;
    /**
     * The file systems user home directory.
     */
    private String userHome;

    public String getPulseHome()
    {
        return pulseHome;
    }

    public String getUserHome()
    {
        return userHome;
    }

    public String execute() throws Exception
    {
        super.execute();
        
        // pulse home
        File f = configurationManager.getHomeDirectory();
        if (f != null)
        {
            pulseHome = f.getAbsolutePath();
            pulseHome = makeJavascriptFriendly(pulseHome);
        }

        // user home
        userHome = System.getProperty("user.home");
        userHome = makeJavascriptFriendly(userHome);

        return SUCCESS;
    }

    /**
     * Required resource.
     *
     * @param configurationManager instance
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

}
