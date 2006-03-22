package com.cinnamonbob.util.logging;

import java.util.logging.Level;

/**
 * Simple wrapper around the java.util.logging.Logger to provide some utility methods.
 */
public class Logger
{
    private java.util.logging.Logger delegate;

    private String source;

    protected Logger(java.util.logging.Logger delegate, String source)
    {
        this.delegate = delegate;
        this.source = source;
    }

    public static Logger getLogger(Class cls)
    {
        return Logger.getLogger(cls.getName());
    }

    public static Logger getLogger(String name)
    {
        return new Logger(java.util.logging.Logger.getLogger(name), name);
    }

    public void severe(String msg, Throwable t)
    {
        delegate.logp(Level.SEVERE, source, "", msg, t);
    }

    public void severe(Throwable t)
    {
        severe(t.getMessage(), t);
    }

    public void severe(String msg)
    {
        severe(msg, null);
    }

    public void info(String msg, Throwable t)
    {
        delegate.logp(Level.INFO, source, "", msg, t);
    }

    public void info(Throwable t)
    {
        info(t.getMessage(), t);
    }

    public void info(String msg)
    {
        info(msg, null);
    }

    public void warning(String msg, Throwable t)
    {
        delegate.logp(Level.WARNING, source, "", msg, t);
    }

    public void warning(Throwable t)
    {
        warning(t.getMessage(), t);
    }

    public void warning(String msg)
    {
        warning(msg, null);
    }

    public void fine(String msg)
    {
        fine(msg, null);
    }

    public void fine(String msg, Throwable t)
    {
        delegate.logp(Level.FINE, source, "", msg, t);
    }

    public void finer(String msg)
    {
        finer(msg, null);
    }

    public void finer(String msg, Throwable t)
    {
        delegate.logp(Level.FINER, source, "", msg, t);
    }

    public void finest(String msg)
    {
        finest(msg, null);
    }

    public void finest(Throwable t)
    {
        finest(t.getMessage(), t);
    }

    public void finest(String msg, Throwable t)
    {
        delegate.logp(Level.FINEST, source, "", msg, t);
    }

    public void debug(String msg)
    {
        finest(msg);
    }

    public void debug(Throwable t)
    {
        finest(t);
    }

    public void debug(String msg, Throwable t)
    {
        finest(msg, t);
    }

    /**
     * Alias for severe.
     * @param msg
     */
    public void error(String msg)
    {
        severe(msg);
    }

    public void error(Throwable t)
    {
        severe(t);
    }

    public void error(String msg, Throwable t)
    {
        severe(msg, t);
    }

    public boolean isLoggable(Level level)
    {
        return delegate.isLoggable(level);
    }

    public void entering(String sourceClass, String sourceMethod)
    {
        delegate.entering(sourceClass, sourceMethod);
    }

    public void exiting(String sourceClass, String sourceMethod)
    {
        delegate.exiting(sourceClass, sourceMethod);
    }

    public void exiting(String sourceClass, String sourceMethod, Object result)
    {
        delegate.exiting(sourceClass, sourceMethod, result);
    }
}
