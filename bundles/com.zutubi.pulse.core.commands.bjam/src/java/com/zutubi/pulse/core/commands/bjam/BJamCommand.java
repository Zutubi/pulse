package com.zutubi.pulse.core.commands.bjam;

import com.zutubi.pulse.core.ProcessArtifact;
import com.zutubi.pulse.core.commands.core.ExecutableCommand;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.CommandResult;

/**
 */
public class BJamCommand extends ExecutableCommand
{
    private String jamfile;
    private String targets;

    public BJamCommand()
    {
        super("bjam.bin", "bjam");
    }

    public void execute(ExecutionContext context, CommandResult cmdResult)
    {
        if (jamfile != null)
        {
            addArguments("-f", jamfile);
            cmdResult.getProperties().put("Jamfile", jamfile);
        }

        if (targets != null)
        {
            addArguments(targets.split(" +"));
            cmdResult.getProperties().put("targets", targets);
        }

        ProcessArtifact pa = createProcess();
        pa.setProcessor(new BJamPostProcessor());

        super.execute(context, cmdResult);
    }

    public String getJamfile()
    {
        return jamfile;
    }

    public void setJamfile(String jamfile)
    {
        this.jamfile = jamfile;
    }

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
    }
}
