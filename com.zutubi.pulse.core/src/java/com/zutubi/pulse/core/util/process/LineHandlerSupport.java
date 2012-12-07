package com.zutubi.pulse.core.util.process;

import java.nio.charset.Charset;

/**
 * Abstract support class for implementing {@link LineHandler}.
 */
public abstract class LineHandlerSupport implements LineHandler
{
    private Charset charset;

    /**
     * Creates a line handler with the default character set.
     */
    protected LineHandlerSupport()
    {
        this(Charset.defaultCharset());
    }

    /**
     * Creates a line handler with the given character set.
     * 
     * @param charset character set to be used to convert output bytes to characters before passing
     *                to this handler
     */
    protected LineHandlerSupport(Charset charset)
    {
        this.charset = charset;
    }

    /**
    * @return the character set to be used to convert output bytes to characters before passing to
     *        this handler
    */
    public Charset getCharset()
    {
        return charset;
    }
}
