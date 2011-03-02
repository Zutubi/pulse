package com.zutubi.pulse.core.util.process;

import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 */
public class BufferingCharHandler extends ForwardingCharHandler
{
    public BufferingCharHandler()
    {
        super(new StringWriter(), new StringWriter());
    }

    public BufferingCharHandler(Charset charset)
    {
        super(charset, new StringWriter(), new StringWriter());
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
