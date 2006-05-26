package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.CoreUserPaths;

import java.io.File;

/**
 */
public class SlaveUserPaths implements CoreUserPaths
{
    private static final String CONFIG_DIR = ".pulse-agent";

    private File pulseData;

    public SlaveUserPaths()
    {
        // TODO: dev-distributed: does this always exist??  Is this even a good idea?
        String home = System.getProperty("user.home");
        File homeDir = new File(home);
        this.pulseData = new File(homeDir, CONFIG_DIR);
    }

    public File getData()
    {
        return pulseData;
    }

    public File getUserConfigRoot()
    {
        return new File(pulseData, "config");
    }
}
