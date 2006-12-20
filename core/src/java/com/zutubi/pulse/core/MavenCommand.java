package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.util.SystemUtils;

import java.io.File;

/**
 * <class-comment/>
 */
public class MavenCommand extends ExecutableCommand
{
    private String targets;

    private void checkExe()
    {
        if (getExe() == null)
        {
            Scope scope = getScope();

            if (scope != null)
            {
                Reference ref = scope.getReference("maven.bin");
                if (ref != null && ref.getValue() instanceof String)
                {
                    setExe((String) ref.getValue());
                }
            }

            // use defaults.
            if (getExe() == null)
            {
                if (SystemUtils.IS_WINDOWS)
                {
                    setExe("maven.bat");
                }
                else
                {
                    setExe("maven");
                }
            }
        }
    }

    public void execute(CommandContext context, CommandResult cmdResult)
    {
        checkExe();

        if (targets != null)
        {
            addArguments(targets.split(" +"));
            cmdResult.getProperties().put("targets", targets);
        }

        super.execute(context, cmdResult);

        MavenUtils.extractVersion(context, cmdResult, new File(getWorkingDir(context.getPaths()), "maven.xml"), "currentVersion");

        StoredArtifact artifact = cmdResult.getArtifact(OUTPUT_ARTIFACT_NAME);
        if(artifact != null)
        {
            MavenPostProcessor pp = new MavenPostProcessor("maven.pp");
            pp.process(artifact.getFile(), cmdResult, context);
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

    public void setScope(Scope scope)
    {
        super.setScope(scope);
        checkExe();
    }
}
