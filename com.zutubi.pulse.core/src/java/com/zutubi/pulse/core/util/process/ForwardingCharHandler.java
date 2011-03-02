package com.zutubi.pulse.core.util.process;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 */
public class ForwardingCharHandler extends CharHandlerSupport
{
    private Writer outWriter;
    private Writer errorWriter;

    public ForwardingCharHandler(Writer outWriter)
    {
        this(outWriter, null);
    }

    public ForwardingCharHandler(Writer outWriter, Writer errorWriter)
    {
        super();
        this.outWriter = outWriter;
        this.errorWriter = errorWriter;
    }

    public ForwardingCharHandler(Charset charset, Writer outWriter)
    {
        this(charset, outWriter, null);
    }

    public ForwardingCharHandler(Charset charset, Writer outWriter, Writer errorWriter)
    {
        super(charset);
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
