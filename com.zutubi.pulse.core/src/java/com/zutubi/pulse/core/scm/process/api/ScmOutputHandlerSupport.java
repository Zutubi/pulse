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

package com.zutubi.pulse.core.scm.process.api;

import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;

/**
 * A helper base class for implementing {@link ScmOutputHandler}.  Adapts
 * feedback to an {@link ScmFeedbackHandler} when one is available.
 */
public class ScmOutputHandlerSupport implements ScmOutputHandler
{
    private ScmFeedbackHandler scmHandler;
    private String commandLine;
    private int exitCode;

    public ScmOutputHandlerSupport()
    {
    }

    public ScmOutputHandlerSupport(ScmFeedbackHandler scmHandler)
    {
        this.scmHandler = scmHandler;
    }

    public void handleCommandLine(String line)
    {
        commandLine = line;
        status(">> " + line);
    }

    /**
     * Returns the command line used to run the external scm process.
     * 
     * @return the scm tool command line
     */
    public String getCommandLine()
    {
        return commandLine;
    }

    public void handleExitCode(int code)
    {
        this.exitCode = code;
    }

    /**
     * Returns the exit code of external scm process.
     * 
     * @return the exit code captured from the external process
     */
    public int getExitCode()
    {
        return exitCode;
    }

    public void checkCancelled() throws ScmCancelledException
    {
        if (scmHandler != null)
        {
            scmHandler.checkCancelled();
        }
    }
    
    protected void status(String status)
    {
        if (scmHandler != null)
        {
            scmHandler.status(status);
        }
    }
}
