package com.zutubi.pulse.core.util.process;

import java.nio.charset.Charset;

/**
 * Abstract support class for implementing {@link LineHandler}.
 */
public abstract class LineHandlerSupport implements LineHandler
{
    private Charset charset;

    protected LineHandlerSupport()
    {
        this(Charset.defaultCharset());
    }

    protected LineHandlerSupport(Charset charset)
    {
        this.charset = charset;
    }

    public Charset getCharset()
    {
        return charset;
    }
}
