package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.util.SystemUtils;

import java.io.File;

/**
 */
public class MavenCommand extends ExecutableCommand
{
    private String targets;

    public MavenCommand()
    {
        super("maven.bin", SystemUtils.IS_WINDOWS ? "maven.bat" : "maven");
    }

    public void execute(ExecutionContext context, CommandResult cmdResult)
    {
        if (targets != null)
        {
            addArguments(targets.split(" +"));
            cmdResult.getProperties().put("targets", targets);
        }

        ProcessArtifact pa = createProcess();
        pa.setProcessor(new MavenPostProcessor("maven.pp"));

        super.execute(context, cmdResult);

        try
        {
            context.setVersion(MavenUtils.extractVersion(new File(getWorkingDir(context.getWorkingDir()), "maven.xml"), "currentVersion"));
        }
        catch (PulseException e)
        {
            cmdResult.warning(e.getMessage());
        }
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
