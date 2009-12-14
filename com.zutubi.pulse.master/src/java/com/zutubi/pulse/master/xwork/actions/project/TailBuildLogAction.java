package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.build.log.BuildLogFile;
import com.zutubi.pulse.master.build.log.LogFile;
import com.zutubi.pulse.master.model.BuildResult;

/**
 * Action to show the end of the build log.
 */
public class TailBuildLogAction extends TailRecipeLogAction
{
    public String execute() throws Exception
    {
        initialiseProperties();

        BuildResult buildResult = getRequiredBuildResult();

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        LogFile buildLog = new BuildLogFile(buildResult, paths);
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
