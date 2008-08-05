package com.zutubi.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that wraps an existing stream and forwards all calls to
 * it except {@link #close}, which is ignored.
 */
public class IgnoreCloseOutputStream extends OutputStream
{
    private OutputStream delegate;

    public IgnoreCloseOutputStream(OutputStream delegate)
    {
        this.delegate = delegate;
    }


    public void write(int b) throws IOException
    {
        delegate.write(b);
    }

    public void write(byte[] b) throws IOException
    {
        delegate.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        delegate.write(b, off, len);
    }

    public void flush() throws IOException
    {
        delegate.flush();
    }

    public void close() throws IOException
    {
        // Ignored, of course.
    }
}
