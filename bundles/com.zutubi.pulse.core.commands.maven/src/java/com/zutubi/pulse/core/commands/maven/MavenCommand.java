package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.MavenUtils;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;
import com.zutubi.pulse.core.postprocessors.api.Feature;

import java.io.File;

/**
 * Support for running Maven 1 - adds automatic version capturing.
 */
public class MavenCommand extends NamedArgumentCommand
{
    public MavenCommand(MavenCommandConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    public void execute(CommandContext commandContext)
    {
        super.execute(commandContext);

        try
        {
            //TODO: use the context's variables to transfer this maven specific information around.
            PulseExecutionContext pec = (PulseExecutionContext) commandContext.getExecutionContext();
            pec.setVersion(MavenUtils.extractVersion(new File(getWorkingDir(pec.getWorkingDir()), "maven.xml"), "currentVersion"));
        }
        catch (PulseException e)
        {
            commandContext.addFeature(new Feature(Feature.Level.WARNING, e.getMessage()));
        }
    }
}
