package com.zutubi.pulse.core.scm.process.api;

/**
 * An SCM output handler that captures stdout and stderr into buffers.
 */
public class ScmOutputCapturingHandler extends ScmOutputHandlerSupport
{
    private String commandLine;
    private StringBuilder stdout = new StringBuilder();
    private StringBuilder stderr = new StringBuilder();

    @Override
    public void handleCommandLine(String line)
    {
        this.commandLine = line;
    }

    @Override
    public void handleStdout(String line)
    {
        stdout.append(line).append('\n');
    }

    @Override
    public void handleStderr(String line)
    {
        stderr.append(line).append('\n');
    }

    /**
     * Returns the command line issued when running the SCM process.
     *
     * @return the command line issued
     */
    public String getCommandLine()
    {
        return commandLine;
    }

    /**
     * Returns the standard output captured from the process.
     *
     * @return the output as a single string
     */
    public String getOutput()
    {
        return stdout.toString();
    }

    /**
     * Returns the standard error captured from the process.
     *
     * @return the error output as a single string
     */
    public String getError()
    {
        return stderr.toString();
    }
}
