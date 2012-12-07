package com.zutubi.pulse.core.util.process;

import java.nio.charset.Charset;

/**
 * A handler that accepts output line-by-line.
 *
 * @see ProcessWrapper
 */
public interface LineHandler
{
    /**
     * Indicates the character set to use for byte to character conversion.
     * 
     * @return the character set used to convert bytes to characters
     */
    Charset getCharset();

    /**
     * Called with a line of output.  Note this method may be called from different threads.
     *
     * @param line the line of output (does not include the line terminator)
     * @param error if false, the line is from standard output, if true it is from standard error
     * @throws Exception on error
     */
    void handle(String line, boolean error) throws Exception;
}
