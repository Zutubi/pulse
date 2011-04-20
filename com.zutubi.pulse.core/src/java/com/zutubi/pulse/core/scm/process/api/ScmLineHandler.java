package com.zutubi.pulse.core.scm.process.api;

/**
 * Interface for SCM output handlers that handle output line by line.
 * 
 * @see ScmLineHandlerSupport
 */
public interface ScmLineHandler extends ScmOutputHandler
{
    /**
     * Called when a line of standard output is read from the process.
     * 
     * @param line the line of output (without the line terminator)
     */
    void handleStdout(String line);

    /**
     * Called when a line of error output is read from the process.
     * 
     * @param line the line of error (without the line terminator)
     */
    void handleStderr(String line);
}
