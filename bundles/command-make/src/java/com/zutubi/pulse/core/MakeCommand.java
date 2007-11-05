package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

/**
 */
public class MakeCommand extends ExecutableCommand
{
    private String makefile;
    private String targets;

    public MakeCommand()
    {
        super("make");
    }

    public void execute(ExecutionContext context, CommandResult cmdResult)
    {
        setExeFromProperty("make.bin");

        if (makefile != null)
        {
            addArguments("-f", makefile);
            cmdResult.getProperties().put("makefile", makefile);
        }

        if (targets != null)
        {
            addArguments(targets.split(" +"));
            cmdResult.getProperties().put("targets", targets);
        }

        super.execute(context, cmdResult);
    }

    public String getMakefile()
    {
        return makefile;
    }

    public void setMakefile(String makefile)
    {
        this.makefile = makefile;
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
