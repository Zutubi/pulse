package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Helper base class for bootstrappers.
 *
 * This helper provides a default implementation of the terminate
 * method.
 */
public abstract class BootstrapperSupport implements Bootstrapper
{
    private volatile boolean terminated = false;
    private transient PrintWriter feedbackWriter;

    public void bootstrap(CommandContext commandContext) throws BuildException
    {
        OutputStream output = commandContext.getExecutionContext().getOutputStream();
        if (output != null)
        {
            feedbackWriter = new PrintWriter(output);
        }

        doBootstrap(commandContext);

        // we don't close the feedback writer because the underlying output stream
        // is not controlled by us.
    }

    protected abstract void doBootstrap(CommandContext commandContext);

    /**
     * Write feedback to the execution contexts output stream.
     *
     * @param msg   the message to be written
     *
     * @see com.zutubi.pulse.core.engine.api.ExecutionContext#getOutputStream()
     */
    protected void writeFeedback(String msg)
    {
        if (feedbackWriter != null)
        {
            feedbackWriter.println(msg);
            feedbackWriter.flush();
        }
    }

    public void terminate()
    {
        terminated = true;
    }

    /**
     * This method indicates whether or not this bootstrapper has been terminated.
     *
     * @return true if {@link #terminate()} has been called, false otherwise.
     */
    public boolean isTerminated()
    {
        return terminated;
    }
}
