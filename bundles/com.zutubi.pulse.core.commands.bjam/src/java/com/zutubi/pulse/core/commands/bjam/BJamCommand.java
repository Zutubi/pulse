package com.zutubi.pulse.core.commands.bjam;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.commands.core.ExecutableCommand;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;

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

        super.execute(context, cmdResult);

        StoredArtifact artifact = cmdResult.getArtifact(OUTPUT_ARTIFACT_NAME);
        if(artifact != null)
        {
            BJamPostProcessor pp = new BJamPostProcessor();
            pp.process(artifact.getFile(), cmdResult, context);
        }
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
