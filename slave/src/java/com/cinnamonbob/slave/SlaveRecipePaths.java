package com.cinnamonbob.slave;

import com.cinnamonbob.ServerRecipePaths;
import com.cinnamonbob.bootstrap.ConfigurationManager;

import java.io.File;

/**
 */
public class SlaveRecipePaths extends ServerRecipePaths
{
    public SlaveRecipePaths(long id, ConfigurationManager configurationManager)
    {
        super(id, configurationManager);
    }

    public File getWorkZip()
    {
        return new File(getWorkDir().getAbsolutePath() + ".zip");
    }

    public File getOutputZip()
    {
        return new File(getOutputDir().getAbsolutePath() + ".zip");
    }
}
