/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.util.io.IgnoreFlushOutputStream;

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
    private static final String PROPERTY_SUPPRESS_OUTPUT = "pulse.suppress.bootstrap.output";

    private volatile boolean terminated = false;
    private transient PrintWriter feedbackWriter;

    public void bootstrap(CommandContext commandContext) throws BuildException
    {
        if (!Boolean.getBoolean(PROPERTY_SUPPRESS_OUTPUT))
        {
            OutputStream output = commandContext.getExecutionContext().getOutputStream();
            if (output != null)
            {
                // Wrap the underlying stream to stop flushing of our writer
                // from being passed all the way through the chain.  The more
                // normal way to handle this is using a buffered writer, but we
                // don't want to buffer at this level because that is handled
                // below.
                feedbackWriter = new PrintWriter(new IgnoreFlushOutputStream(output));
            }
        }

        doBootstrap(commandContext);

        // we don't close the feedback writer because the underlying output stream
        // is not controlled by us.
        if (feedbackWriter != null)
        {
            feedbackWriter.flush();
        }
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
