package com.zutubi.pulse.master;

import com.zutubi.pulse.master.model.BuildResult;

import java.io.File;

/**
 *
 *
 */
public class DefaultBuildLogger extends AbstractOutputLogger implements BuildLogger
{
    private static final String PRE_MARKER = "============================[ task output below ]============================";
    private static final String POST_MARKER = "============================[ task output above ]============================";

    public DefaultBuildLogger(File logFile)
    {
        super(logFile);
    }

    public void prepare()
    {
        openWriter();
    }

    public void preBuild()
    {
        logMarker("Running pre build hooks...");
    }

    public void preBuildCompleted()
    {
        logMarker("Pre build hooks complete.");
    }

    public void hookCommenced(String name)
    {
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
        logMarker("Running post build hooks...");
    }

    public void postBuildCompleted()
    {
        logMarker("Post build hooks complete.");
    }

    public void done()
    {
        closeWriter();
    }
}
