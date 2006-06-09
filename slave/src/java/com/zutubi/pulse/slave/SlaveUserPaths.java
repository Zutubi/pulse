package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.UserPaths;
import com.zutubi.pulse.bootstrap.SystemPaths;

import java.io.File;

/**
 */
public class SlaveUserPaths implements UserPaths
{
    private static final String CONFIG_DIR = ".pulse-agent";

    private File data;
    private File userConfigRoot;

    public SlaveUserPaths(SystemPaths systemPaths)
    {
        data = new File(systemPaths.getSystemRoot(), "data");

        String home = System.getProperty("user.home");
        File homeDir = new File(home);
        this.userConfigRoot = new File(homeDir, CONFIG_DIR);
    }

    public File getData()
    {
        return data;
    }

    public File getUserConfigRoot()
    {
        return userConfigRoot;
    }
}
