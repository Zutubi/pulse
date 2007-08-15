package com.zutubi.pulse.core.scm.cvs.client;

import com.zutubi.util.logging.Logger;

import java.io.OutputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * <class comment/>
 */
public class LoggingOutputStream extends OutputStream
{
    private Logger delegate;
    private Level level;

    private StringBuilder builder;

    private final Object lock = new Object();

    public LoggingOutputStream(Logger delegate, Level level)
    {
        this.delegate = delegate;
        this.level = level;

        builder = new StringBuilder();
    }

    public void write(int b) throws IOException
    {
        synchronized(lock)
        {
            builder.append(new String(new byte[]{(byte)b}));
        }
    }

    public void flush() throws IOException
    {
        String msg;
        synchronized(lock)
        {
            msg = builder.toString();
            builder = new StringBuilder();
        }
        delegate.log(level, msg);
    }
}
