package com.zutubi.util;

import java.io.OutputStream;
import java.io.IOException;

/**
 * An output stream that forwards all writes to multiple output streams.
 */
public class ForkOutputStream extends OutputStream
{
    private OutputStream[] streams;

    public ForkOutputStream(OutputStream... streams)
    {
        this.streams = streams;
    }

    public void write(int b) throws IOException
    {
        for(OutputStream o: streams)
        {
            o.write(b);
        }
    }

    @Override
    public void write(byte b[]) throws IOException
    {
        for(OutputStream o: streams)
        {
            o.write(b);
        }
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException
    {
        for(OutputStream o: streams)
        {
            o.write(b, off, len);
        }
    }

    @Override
    public void flush() throws IOException
    {
        for(OutputStream o: streams)
        {
            o.flush();
        }
    }

    @Override
    public void close() throws IOException
    {
        for(OutputStream o: streams)
        {
            o.close();
        }
    }
}
