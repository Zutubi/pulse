package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.engine.AbstractCommandConfiguration;
import com.zutubi.pulse.core.engine.api.FileLoadException;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Required;
import com.zutubi.util.TextUtils;


/**
 * A command group represents a command and a set of artifact definitions.
 */
@SymbolicName("zutubi.commandGroup")
public class CommandGroupConfiguration extends AbstractCommandConfiguration implements Validateable
{
    @Required
    private AbstractCommandConfiguration command = null;

    public boolean isForce()
    {
        return super.isForce() || command.isForce();
    }

    public AbstractCommandConfiguration getCommand()
    {
        return command;
    }

    public void setCommand(AbstractCommandConfiguration command)
    {
        if (getName() == null)
        {
            setName(command.getName());
        }
        this.command = command;
    }

    /**
     * Validate the configuration of this command group, ensuring that the artifact names are
     * unique.
     *
     * @param context instance.
     */
    public void validate(ValidationContext context)
    {
        // ensure that our artifacts have unique names.
        // FIXME loader
//        List<String> artifactNames = getArtifactNames();
//        Set<String> names = new TreeSet<String>();
//        for (String name : artifactNames)
//        {
//            if (names.contains(name))
//            {
//                context.addFieldError("name", "A duplicate artifact name '" + name + "' has been detected. Please only " +
//                        "use unique names for artifacts within a command group.");
//            }
//            names.add(name);
//        }
    }

    public Command createCommand()
    {
        CommandGroup commandGroup = new CommandGroup();
        commandGroup.setName(command.getName());
        try
        {
            commandGroup.add(command.createCommand());
        }
        catch (FileLoadException e)
        {
// FIXME loader
            e.printStackTrace();
        }
        return commandGroup;
    }
}