package com.zutubi.pulse.core.commands.ant;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.core.NamedArgumentCommandSupport;

/**
 */
public class AntCommand extends NamedArgumentCommandSupport<AntCommandConfiguration>
{
    public AntCommand(AntCommandConfiguration config)
    {
        super(config);
    }

    public void execute(CommandContext commandContext)
    {
        super.execute(commandContext);
        // FIXME loader
//        commandContext.processOutput(OUTPUT_NAME, new AntPostProcessor());
    }
}
