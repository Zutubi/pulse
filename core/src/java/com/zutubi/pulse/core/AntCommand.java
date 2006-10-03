package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.util.SystemUtils;

/**
 */
public class AntCommand extends ExecutableCommand
{
    private String buildFile;
    private String targets;

    private void checkExe()
    {
        if (getExe() == null)
        {
            Scope scope = getScope();
            if (scope != null)
            {
                Reference ref = scope.getReference("ant.bin");
                if (ref != null && ref.getValue() instanceof String)
                {
                    setExe((String) ref.getValue());
                }
            }

            if (getExe() == null)
            {
                if (SystemUtils.isWindows())
                {
                    setExe("ant.bat");
                }
                else
                {
                    setExe("ant");
                }
            }
        }
    }

    public void execute(CommandContext context, CommandResult cmdResult)
    {
        checkExe();

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

        super.execute(context, cmdResult);

        AntPostProcessor pp = new AntPostProcessor("ant.pp");
        pp.process(cmdResult.getArtifact(OUTPUT_NAME).getFile(), cmdResult, context);
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

    public void setScope(Scope scope)
    {
        super.setScope(scope);
        checkExe();
    }
}
