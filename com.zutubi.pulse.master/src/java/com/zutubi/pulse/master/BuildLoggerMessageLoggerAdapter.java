package com.zutubi.pulse.master;

import static com.zutubi.pulse.core.dependency.ivy.IvyLogUtils.PROGRESS_CHARACTER;
import static com.zutubi.pulse.core.dependency.ivy.IvyLogUtils.toLevel;
import org.apache.ivy.util.AbstractMessageLogger;

import java.util.logging.Level;

/**
 * A message logger implementation that redirects ivy logging to a build logger.
 */
public class BuildLoggerMessageLoggerAdapter extends AbstractMessageLogger
{
    /**
     * The default level at which we are logging ivy messages to
     * the build logger.
     */
    private static final Level DEFAULT_LOG_LEVEL = Level.INFO;

    private BuildLogger buildLogger;

    public BuildLoggerMessageLoggerAdapter(BuildLogger buildLogger)
    {
        this.buildLogger = buildLogger;
    }

    protected void doProgress()
    {
        buildLogger.log(PROGRESS_CHARACTER);
    }

    protected void doEndProgress(String msg)
    {
        buildLogger.status(msg);
    }

    public void rawlog(String msg, int level)
    {
        log(msg, level);
    }

    public void log(String msg, int level)
    {
        if (isLoggable(toLevel(level)))
        {
            buildLogger.status(msg);
        }
    }

    private boolean isLoggable(Level level)
    {
        if (level.intValue() < DEFAULT_LOG_LEVEL.intValue() || DEFAULT_LOG_LEVEL == Level.OFF)
        {
            return false;
        }
        return true;
    }

}
