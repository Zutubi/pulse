package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.util.AbstractMessageLogger;

/**
 * An implementation of the ivy message logger interface that ignores all log requests.
 */
public class NullMessageLogger extends AbstractMessageLogger
{
    protected void doProgress()
    {

    }

    protected void doEndProgress(String msg)
    {

    }

    public void log(String msg, int level)
    {

    }

    public void rawlog(String msg, int level)
    {

    }
}
