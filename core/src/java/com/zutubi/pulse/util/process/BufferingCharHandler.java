package com.zutubi.pulse.util.process;

import java.io.StringWriter;

/**
 */
public class BufferingCharHandler extends ForwardingCharHandler
{
    public BufferingCharHandler()
    {
        super(new StringWriter(), new StringWriter());
    }

    public String getStdout()
    {
        return ((StringWriter)getOutWriter()).getBuffer().toString();
    }

    public String getStderr()
    {
        return ((StringWriter)getErrorWriter()).getBuffer().toString();
    }
}
