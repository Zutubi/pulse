package com.zutubi.pulse.core.util.process;

import java.nio.charset.Charset;

/**
 * Abstract support class for implementing {@link CharHandler}.
 */
public abstract class CharHandlerSupport implements CharHandler
{
    private Charset charset;

    protected CharHandlerSupport()
    {
        this(Charset.defaultCharset());
    }

    protected CharHandlerSupport(Charset charset)
    {
        this.charset = charset;
    }

    public Charset getCharset()
    {
        return charset;
    }
}
