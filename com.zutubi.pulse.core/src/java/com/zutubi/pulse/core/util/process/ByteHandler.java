package com.zutubi.pulse.core.util.process;

/**
 * A handler that accepts output as raw bytes.
 * 
 * @see ProcessWrapper
 */
public interface ByteHandler
{
    /**
     * Called with a chunk of output.  Note this method may be called from different threads.
     * 
     * @param buffer array holding the output data
     * @param n number of bytes available in the array (starting from the beginning)
     * @param error if false, the data is from standard output, if true it is from standard error
     * @throws Exception on error
     */
    void handle(byte[] buffer, int n, boolean error) throws Exception;
}
