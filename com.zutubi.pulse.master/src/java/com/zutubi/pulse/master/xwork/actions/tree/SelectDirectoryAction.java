package com.zutubi.pulse.master.xwork.actions.tree;

import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

import java.io.File;

/**
 * This action provides the data needed to drive the select directory popup.
 *
 * In particular, it provides the PULSE_HOME directory and the USER_HOME directory
 * to allow the UI to provide shortcuts to those locations.
 */
public class SelectDirectoryAction extends ActionSupport
{
    private MasterConfigurationManager configurationManager;

    /**
     * The pulse home directory.
     */
    private String pulseHome;

    /**
     * The user home directory.
     */
    private String userHome;

    /**
     * The path separator used by the UI when it composes a path from nodes.
     */
    private String separator;

    public String getPulseHome()
    {
        return pulseHome;
    }

    public String getUserHome()
    {
        return userHome;
    }

    public String getSeparator()
    {
        return separator;
    }

    public String execute() throws Exception
    {
        // pulse home
        File f = configurationManager.getHomeDirectory();
        if (f != null)
        {
            pulseHome = f.getAbsolutePath();
            pulseHome = makeJavascriptFriendly(pulseHome);
        }

        // user home
        userHome = System.getProperty(EnvConfig.USER_HOME);
        userHome = makeJavascriptFriendly(userHome);

        separator = makeJavascriptFriendly(File.separator);

        return SUCCESS;
    }

    /**
     * The '\' character is the javascript escape character. It is also the separator character on
     * some systems. When we render a path that contains this character, the path is corrupted with the
     * '\' escaping the first character of the files it separates. SO, to fix this, we escape it if
     * necessary.
     *
     * @param str
     *
     * @return a version of str with the '\' escaped.
     */
    private String makeJavascriptFriendly(String str)
    {
        return str.replace("\\", "\\\\");
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
}
