package com.zutubi.pulse.core.util.process;

import java.io.OutputStream;

/**
 */
public class ForwardingByteHandler implements ByteHandler
{
    private OutputStream outStream;
    private OutputStream errorStream;

    public ForwardingByteHandler(OutputStream outStream)
    {
        this.outStream = outStream;
    }

    public ForwardingByteHandler(OutputStream outStream, OutputStream errorStream)
    {
        this.outStream = outStream;
        this.errorStream = errorStream;
    }

    public void handle(byte[] buffer, int n, boolean error) throws Exception
    {
        if(error)
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
