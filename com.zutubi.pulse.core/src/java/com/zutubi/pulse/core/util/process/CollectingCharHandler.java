package com.zutubi.pulse.core.util.process;

import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * A character handler that collects all output in memory for later access.
 */
public class CollectingCharHandler extends ForwardingCharHandler
{
    /**
     * Creates a new collecting handler with the default character set.
     */
    public CollectingCharHandler()
    {
        super(new StringWriter(), new StringWriter());
    }

    /**
     * Creates a new collecting handler with the given character set.
     * 
     * @param charset the character set to be used to convert output bytes to characters
     */
    public CollectingCharHandler(Charset charset)
    {
        super(charset, new StringWriter(), new StringWriter());
    }

    /**
     * @return all standard output that has been collected
     */
    public String getStdout()
    {
        return ((StringWriter)getOutWriter()).getBuffer().toString();
    }

    /**
     * @return all standard error that has been collected
     */
    public String getStderr()
    {
        return ((StringWriter)getErrorWriter()).getBuffer().toString();
    }
}
