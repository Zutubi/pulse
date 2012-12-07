package com.zutubi.pulse.core.util.process;

import java.io.OutputStream;

/**
 * Adapts from the {@link ByteHandler} interface to a pair of {@link OutputStream} instances, one
 * each for standard output and standard error.
 */
public class ForwardingByteHandler implements ByteHandler
{
    private OutputStream outStream;
    private OutputStream errorStream;

    /**
     * Creates a handler that only forwards standard output, ignoring standard error.
     * 
     * @param outStream the stream that will be passed standard output
     */
    public ForwardingByteHandler(OutputStream outStream)
    {
        this.outStream = outStream;
    }

    /**
     * Creates a handler that forwards both standard output and standard error.
     *
     * @param outStream the stream that will be passed standard output
     * @param errorStream the stream that will be passed standard error
     */
    public ForwardingByteHandler(OutputStream outStream, OutputStream errorStream)
    {
        this.outStream = outStream;
        this.errorStream = errorStream;
    }

    public void handle(byte[] buffer, int n, boolean error) throws Exception
    {
        if (error)
        {
            if (errorStream != null)
            {
                errorStream.write(buffer, 0, n);
            }
        }
        else
        {
            outStream.write(buffer, 0, n);
        }
    }
}
