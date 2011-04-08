package com.zutubi.pulse.core.scm.process.api;

import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmException;

/**
 * Callback interface used for progress reporting when running an external SCM
 * tool.
 * 
 * @see ScmOutputHandlerSupport
 */
public interface ScmOutputHandler
{
    /**
     * Called just before starting the child process with the command line
     * that will be used to invoke it.
     *  
     * @param line command line, starting with the binary path and with
     *             space-separated arguments
     */
    void handleCommandLine(String line);

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

    /**
     * Called when the process has just exited, with the exit code it
     * returned.
     * 
     * @param code the exit code of the process
     * @throws ScmException on any error
     */
    void handleExitCode(int code) throws ScmException;

    /**
     * Called periodically to check if the operation should be cancelled.  If
     * this method determines that a cancel is required, it should throw an
     * {@link com.zutubi.pulse.core.scm.api.ScmCancelledException}.
     * 
     * @throws com.zutubi.pulse.core.scm.api.ScmCancelledException when a cancel is required
     */
    void checkCancelled() throws ScmCancelledException;
}
