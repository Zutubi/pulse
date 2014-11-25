package com.zutubi.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that wraps a delegate and closes it when there is no more data to read.
 */
public class AutoCloseInputStream extends InputStream
{
    private InputStream delegate;
    private boolean closed = false;

    public AutoCloseInputStream(InputStream delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public int read() throws IOException
    {
        int result = -1;
        if (!closed)
        {
            result = delegate.read();
            if (result == -1)
            {
                close();
            }
        }

        return result;
    }

    @Override
    public void close() throws IOException
    {
        if (!closed)
        {
            closed = true;
            delegate.close();
        }
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return delegate.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        return delegate.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException
    {
        return delegate.skip(n);
    }

    @Override
    public int available() throws IOException
    {
        return delegate.available();
    }

    @Override
    public void mark(int readlimit)
    {
        delegate.mark(readlimit);
    }

    @Override
    public void reset() throws IOException
    {
        delegate.reset();
    }

    @Override
    public boolean markSupported()
    {
        return delegate.markSupported();
    }
}
