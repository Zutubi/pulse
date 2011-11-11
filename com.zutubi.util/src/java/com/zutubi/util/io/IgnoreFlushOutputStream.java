package com.zutubi.util.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that wraps an existing stream and forwards all calls to
 * it except {@link #flush}, which is ignored.
 */
public class IgnoreFlushOutputStream extends OutputStream
{
    private OutputStream delegate;

    public IgnoreFlushOutputStream(OutputStream delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void write(int i) throws IOException
    {
        delegate.write(i);
    }

    @Override
    public void write(byte[] bytes) throws IOException
    {
        delegate.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException
    {
        delegate.write(bytes, i, i1);
    }

    @Override
    public void flush() throws IOException
    {
        // Ignored.
    }

    @Override
    public void close() throws IOException
    {
        delegate.close();
    }
}
