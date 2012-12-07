package com.zutubi.pulse.core.util.process;

import java.nio.charset.Charset;

/**
 * A handler that accepts output as characters.
 *
 * @see ProcessWrapper
 */
public interface CharHandler
{
    /**
     * Indicates the character set to use for byte to character conversion.
     * 
     * @return the character set used to convert bytes to characters
     */
    Charset getCharset();

    /**
     * Called with a chunk of output.  Note this method may be called from different threads.
     *
     * @param buffer array holding the output data
     * @param n number of characters available in the array (starting from the beginning)
     * @param error if false, the data is from standard output, if true it is from standard error
     * @throws Exception on error
     */
    void handle(char[] buffer, int n, boolean error) throws Exception;
}
