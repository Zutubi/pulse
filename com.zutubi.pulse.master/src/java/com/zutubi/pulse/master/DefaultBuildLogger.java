package com.zutubi.pulse.master;

import com.zutubi.pulse.master.model.BuildResult;

import java.io.File;

public class DefaultBuildLogger extends AbstractFileLogger implements BuildLogger
{
    private static final String PRE_MARKER = "============================[ task output below ]============================";
    private static final String POST_MARKER = "============================[ task output above ]============================";

    private int hookCount = 0;

    public DefaultBuildLogger(File logFile)
    {
        super(logFile);
    }

    public void preBuild()
    {
        hookCount = 0;
        logMarker("Running pre build hooks...");
    }

    public void preBuildCompleted()
    {
        logMarker(String.format("Pre build hooks complete (%d hook%s run).", hookCount, hookCount == 1 ? "" : "s"));
    }

    public void hookCommenced(String name)
    {
        hookCount++;
        logMarker("Hook '" + name + "' commenced");
        if (writer != null)
        {
            writer.println(PRE_MARKER);
            writer.flush();
        }
    }

    public void hookCompleted(String name)
    {
        if (writer != null)
        {
            completeOutput();
            writer.println(POST_MARKER);
            writer.flush();
        }
        logMarker("Hook '" + name + "' completed");
    }

    public void commenced(BuildResult build)
    {
        logMarker("Build commenced", build.getStamps().getStartTime());
    }

    public void status(String message)
    {
        logMarker(message);
    }

    public void completed(BuildResult build)
    {
        logMarker("Build completed with status " + build.getState().getPrettyString(), build.getStamps().getEndTime());
    }

    public void postBuild()
    {
        hookCount = 0;
        logMarker("Running post build hooks...");
    }

    public void postBuildCompleted()
    {
        logMarker(String.format("Post build hooks complete (%d hook%s run).", hookCount, hookCount == 1 ? "" : "s"));
    }
}
