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
