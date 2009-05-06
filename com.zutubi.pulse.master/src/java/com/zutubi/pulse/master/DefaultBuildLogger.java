package com.zutubi.pulse.master;

import com.zutubi.pulse.core.dependency.ivy.IvyMessageOutputStreamAdapter;
import static com.zutubi.pulse.core.dependency.ivy.IvyUtils.toLevel;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.util.AbstractMessageLogger;
import org.apache.ivy.util.MessageLogger;

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

    public void preIvyPublish()
    {
        logMarker("Running ivy publish...");
    }

    public void postIvyPublish()
    {
        logMarker("Ivy publish complete.");
    }

    /**
     * Get an adapter to allow this build logger to receive messages ment for an ivy
     * message logger.
     *
     * @return a message logger instance that delegates logging to this build logger instance.
     */
    public MessageLogger getMessageLogger()
    {
        return new AbstractMessageLogger()
        {
            private final Logger LOG = Logger.getLogger(IvyMessageOutputStreamAdapter.class);

            protected void doProgress()
            {
                // noop.
            }

            protected void doEndProgress(String msg)
            {
                // noop.
            }

            public void rawlog(String msg, int level)
            {
                log(msg, level);
            }

            public void log(String msg, int level)
            {
                if (isLoggable(level))
                {
                    status(msg);
                }
            }

            private boolean isLoggable(int level)
            {
                // delegate the checks for isLoggable to teh IvyMessageOutputStreamAdapter, so
                // that the configuration of the ivy message log level can be controlled in one location.
                return LOG.isLoggable(toLevel(level));
            }
        };
    }
}
