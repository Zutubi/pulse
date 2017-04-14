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

package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.process.api.ScmLineHandler;
import com.zutubi.pulse.core.ui.api.UserInterface;

/**
 * A handler for the p4 sync operation that just passes the output through to
 * the UI.
 */
public class PerforceSyncFeedbackHandler implements ScmLineHandler
{
    private UserInterface ui;
    private String commandLine;
    private boolean resolveRequired = false;
    private boolean errorEncountered = false;

    public PerforceSyncFeedbackHandler(UserInterface ui)
    {
        this.ui = ui;
    }

    public void handleCommandLine(String line)
    {
        commandLine = line;
    }

    public void handleStdout(String line)
    {
        if(ui != null)
        {
            if(line.startsWith("...") && line.contains("must resolve"))
            {
                resolveRequired = true;
            }
            
            ui.status(line);
        }
    }

    public void handleStderr(String line)
    {
        if(ui != null)
        {
            if(line.contains("ile(s) up-to-date."))
            {
                ui.status(line);
            }
            else
            {
                ui.error(line);
                errorEncountered = true;
            }
        }
    }

    public void handleExitCode(int code) throws ScmException
    {
        String command = commandLine == null ? "p4 process" : "'" + commandLine + "'";
        if(code != 0)
        {
            throw new ScmException(command + " returned non-zero exit code: " + code);
        }
        else if(errorEncountered)
        {
            throw new ScmException(command + " reported errors");
        }
    }

    public void checkCancelled() throws ScmCancelledException
    {
    }

    public boolean isResolveRequired()
    {
        return resolveRequired;
    }
}
