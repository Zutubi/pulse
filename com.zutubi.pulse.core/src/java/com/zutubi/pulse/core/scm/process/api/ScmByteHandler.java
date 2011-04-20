package com.zutubi.pulse.core.scm.process.api;

/**
 * Interface for SCM output handlers that handle output as byte buffers.
 */
public interface ScmByteHandler extends ScmOutputHandler
{
    /**
     * Called when a line of standard output is read from the process.
     * 
     * @param buffer buffer holding the output bytes
     * @param n      number of bytes of output in the buffer
     */
    void handleStdout(byte[] buffer, int n);

    /**
     * Called when a line of error output is read from the process.
     * 
     * @param buffer buffer holding the output bytes
     * @param n      number of bytes of output in the buffer
     */
    void handleStderr(byte[] buffer, int n);
}
