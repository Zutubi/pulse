package com.zutubi.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that wraps another stream and performs some custom cleanup
 * on close.
 */
public class CleanupInputStream extends InputStream
{
    private InputStream delegate;
    private CleanupCallback callback;

    public CleanupInputStream(InputStream delegate, CleanupCallback callback)
    {
        this.delegate = delegate;
        this.callback = callback;
    }

    public int read() throws IOException
    {
        return delegate.read();
    }

    public int read(byte[] b) throws IOException
    {
        return delegate.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException
    {
        return delegate.read(b, off, len);
    }

    public long skip(long n) throws IOException
    {
        return delegate.skip(n);
    }

    public int available() throws IOException
    {
        return delegate.available();
    }

    public void close() throws IOException
    {
        try
        {
            delegate.close();
        }
        finally
        {
            callback.execute();
        }
    }

    public void mark(int readlimit)
    {
        delegate.mark(readlimit);
    }

    public void reset() throws IOException
    {
        delegate.reset();
    }

    public boolean markSupported()
    {
        return delegate.markSupported();
    }

    public interface CleanupCallback
    {
        void execute();
    }
}
