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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.OutputProducingCommandSupport;
import com.zutubi.util.io.IOUtils;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * A command that prints a message to stdout.
 */
public class PrintCommand extends OutputProducingCommandSupport
{
    private boolean terminated = false;

    public PrintCommand(PrintCommandConfiguration config)
    {
        super(config);
    }

    @Override
    public PrintCommandConfiguration getConfig()
    {
        return (PrintCommandConfiguration) super.getConfig();
    }

    public void execute(CommandContext commandContext, OutputStream outputStream)
    {
        if(terminated)
        {
            commandContext.error("Terminated");
            return;
        }

        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(outputStream);
            writer.write(getConfig().getMessage());
            if(getConfig().isAddNewline())
            {
                writer.println();
            }
        }
        finally
        {
            IOUtils.close(writer);
        }
    }
    
    public void terminate()
    {
        terminated = true;
    }
}
