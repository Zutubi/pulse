package com.cinnamonbob.bootstrap;

/**
 * 
 *
 */
public class StartupException extends RuntimeException
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
