/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.util.SystemUtils;

import java.io.File;

/**
 * <class-comment/>
 */
public class Maven2Command extends ExecutableCommand implements Command, ScopeAware
{
    private Scope scope;
    private String targets = "test";

    private void checkExe()
    {
        if (getExe() == null)
        {
            if (scope != null)
            {
                Reference ref = scope.getReference("maven2.bin");
                if (ref != null && ref.getValue() instanceof String)
                {
                    setExe((String) ref.getValue());
                }
            }

            // use defaults.
            if (getExe() == null)
            {
                if (SystemUtils.isWindows())
                {
                    setExe("mvn.bat");
                }
                else
                {
                    setExe("mvn");
                }
            }
        }
    }

    public void execute(long recipeId, RecipePaths paths, File outputDir, CommandResult cmdResult)
    {
        checkExe();

        if (targets != null)
        {
            addArguments(targets.trim().split(" +"));
            cmdResult.getProperties().put("targets", targets);
        }

        super.execute(recipeId, paths, outputDir, cmdResult);

        Maven2PostProcessor pp = new Maven2PostProcessor("maven.pp");
        pp.process(outputDir, cmdResult.getArtifact(OUTPUT_NAME).getFile(), cmdResult);
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
        this.scope = scope;
        checkExe();
    }
}
