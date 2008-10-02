package com.zutubi.pulse.web.project;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.master.model.BuildResult;

import java.io.File;

/**
 *
 *
 */
public class TailBuildLogAction extends TailRecipeLogAction
{
    public String execute() throws Exception
    {
        initialiseProperties();

        BuildResult buildResult = getRequiredBuildResult();

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        File buildLog = new File(paths.getBuildDir(buildResult), "build.log");
        if (buildLog.exists())
        {
            logExists = true;
            if (raw)
            {
                return getRaw(buildLog);
            }
            else
            {
                return getTail(buildLog);
            }
        }
        else
        {
            logExists = false;
        }
        return "tail";
    }

}
