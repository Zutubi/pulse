package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.util.SystemUtils;

/**
 */
public class AntCommand extends ExecutableCommand
{
    private String buildFile;
    private String targets;

    public AntCommand()
    {
        super(SystemUtils.IS_WINDOWS ? "ant.bat" : "ant");
    }

    public void execute(ExecutionContext context, CommandResult cmdResult)
    {
        setExeFromProperty("ant.bin");

        if (buildFile != null)
        {
            addArguments("-f", buildFile);
            cmdResult.getProperties().put("build file", buildFile);
        }

        if (targets != null)
        {
            addArguments(targets.split(" +"));
            cmdResult.getProperties().put("targets", targets);
        }

        ProcessArtifact pa = createProcess();
        pa.setProcessor(new AntPostProcessor("ant.pp"));

        super.execute(context, cmdResult);
    }

    public String getBuildFile()
    {
        return buildFile;
    }

    public void setBuildFile(String buildFile)
    {
        this.buildFile = buildFile;
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
