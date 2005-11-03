package com.cinnamonbob.bootstrap;

import com.cinnamonbob.core.BobRuntimeException;

/**
 * 
 *
 */
public class StartupException extends BobRuntimeException
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
