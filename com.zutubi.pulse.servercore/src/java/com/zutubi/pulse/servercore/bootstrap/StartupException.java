package com.zutubi.pulse.servercore.bootstrap;

import com.zutubi.pulse.core.PulseRuntimeException;

/**
 * 
 *
 */
public class StartupException extends PulseRuntimeException
{
    public StartupException()
    {
        super();
    }

    public StartupException(String message)
    {
        super(message);
    }

    public StartupException(String message, Throwable throwable)
    {
        super(message, throwable);
    }

    public StartupException(Throwable throwable)
    {
        super(throwable);
    }
}
