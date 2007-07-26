package com.zutubi.pulse.util.process;

import java.io.IOException;
import java.io.Writer;

/**
 */
public class ForwardingCharHandler implements CharHandler
{
    private Writer outWriter;
    private Writer errorWriter;

    public ForwardingCharHandler(Writer outWriter)
    {
        this.outWriter = outWriter;
    }

    public ForwardingCharHandler(Writer outWriter, Writer errorWriter)
    {
        this.outWriter = outWriter;
        this.errorWriter = errorWriter;
    }

    public void handle(char[] buffer, int n, boolean error) throws IOException
    {
        if(error)
        {
            if(errorWriter != null)
            {
                errorWriter.write(buffer, 0, n);
            }
        }
        else
        {
            outWriter.write(buffer, 0, n);
        }
    }

    public Writer getOutWriter()
    {
        return outWriter;
    }

    public Writer getErrorWriter()
    {
        return errorWriter;
    }
}
