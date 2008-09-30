package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.UserPaths;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 */
public class SlaveUserPaths implements UserPaths
{
    private static final Logger LOG = Logger.getLogger(SlaveUserPaths.class);

    private File data;
    private File userConfigRoot;

    public SlaveUserPaths(DefaultSlaveConfiguration appConfig)
    {
        data = new File(appConfig.getDataPath());
        userConfigRoot = new File(data, "config");

        if(!userConfigRoot.isDirectory())
        {
            if(!userConfigRoot.mkdirs())
            {
                LOG.severe("Unable to create user config directory '" + userConfigRoot.getAbsolutePath() + "'");
            }
        }
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
