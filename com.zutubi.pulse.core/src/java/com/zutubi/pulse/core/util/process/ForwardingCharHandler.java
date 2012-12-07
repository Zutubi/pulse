package com.zutubi.pulse.core.util.process;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Adapts from the {@link CharHandler} interface to a pair of {@link java.io.Writer} instances, one
 * each for standard output and standard error.
 */
public class ForwardingCharHandler extends CharHandlerSupport
{
    private Writer outWriter;
    private Writer errorWriter;

    /**
     * Creates a handler with the default character set that only forwards standard output, ignoring
     * standard error.
     *
     * @param outWriter the writer that will be passed standard output
     */
    public ForwardingCharHandler(Writer outWriter)
    {
        this(outWriter, null);
    }

    /**
     * Creates a handler with the default character set that forwards both standard output and
     * standard error.
     *
     * @param outWriter the writer that will be passed standard output
     * @param errorWriter the writer that will be passed standard error
     */
    public ForwardingCharHandler(Writer outWriter, Writer errorWriter)
    {
        super();
        this.outWriter = outWriter;
        this.errorWriter = errorWriter;
    }

    /**
     * Creates a handler with the given character set that only forwards standard output, ignoring
     * standard error.
     *
     * @param charset character set to be used to convert output to characters before it is passed
     *                to this
     * @param outWriter the writer that will be passed standard output
     */
    public ForwardingCharHandler(Charset charset, Writer outWriter)
    {
        this(charset, outWriter, null);
    }

    /**
     * Creates a handler with the given character set that forwards both standard output and
     * standard error.
     *
     * @param charset character set to be used to convert output to characters before it is passed
     *                to this
     * @param outWriter the writer that will be passed standard output
     * @param errorWriter the writer that will be passed standard error
     */
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

    /**
     * @return the writer that handles standard output
     */
    public Writer getOutWriter()
    {
        return outWriter;
    }

    /**
     * @return the writer that handles standard error, may be null if it is not being handled
     */
    public Writer getErrorWriter()
    {
        return errorWriter;
    }
}
