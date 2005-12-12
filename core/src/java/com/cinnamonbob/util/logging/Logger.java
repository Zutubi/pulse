package com.cinnamonbob.util.logging;

import java.util.logging.Level;

/**
 * Simple wrapper around the java.util.logging.Logger to provide some utility methods.
 */
public class Logger
{
    private java.util.logging.Logger delegate;

    protected Logger(java.util.logging.Logger delegate)
    {
        this.delegate = delegate;
    }

    public static Logger getLogger(String name)
    {
        return new Logger(java.util.logging.Logger.getLogger(name));
    }

    public void severe(String msg, Throwable t)
    {
        delegate.log(Level.SEVERE, msg, t);
    }

    public void severe(String msg)
    {
        delegate.severe(msg);
    }

    public void info(String msg, Throwable t)
    {
        delegate.log(Level.INFO, msg, t);
    }

    public void info(String msg)
    {
        delegate.info(msg);
    }

    public void warning(String msg, Throwable t)
    {
        delegate.log(Level.WARNING, msg, t);
    }

    public void warning(String msg)
    {
        delegate.warning(msg);
    }

    public void fine(String msg)
    {
        delegate.fine(msg);
    }

    public void finer(String msg)
    {
        delegate.finer(msg);
    }

    public void finest(String msg)
    {
        delegate.finest(msg);
    }

    public boolean isLoggable(Level level)
    {
        return delegate.isLoggable(level);
    }
}
