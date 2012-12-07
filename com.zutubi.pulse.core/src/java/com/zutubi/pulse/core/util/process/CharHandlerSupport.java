package com.zutubi.pulse.core.util.process;

import java.nio.charset.Charset;

/**
 * Abstract support class for implementing {@link CharHandler}.
 */
public abstract class CharHandlerSupport implements CharHandler
{
    private Charset charset;

    /**
     * Creates a handler with the default character set.
     */
    protected CharHandlerSupport()
    {
        this(Charset.defaultCharset());
    }

    /**
     * Creates a handler with the specified character set for output byte to character conversion.
     * 
     * @param charset the character set that should be used to convert output before it is passed to
     *                this handler
     */
    protected CharHandlerSupport(Charset charset)
    {
        this.charset = charset;
    }

    /**
     * @return the character set that should be used to convert output before it is passed to this
     *         handler
     */
    public Charset getCharset()
    {
        return charset;
    }
}
