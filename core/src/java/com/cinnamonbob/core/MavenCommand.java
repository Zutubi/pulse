package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.util.SystemUtils;

import java.io.File;

/**
 * <class-comment/>
 */
public class MavenCommand extends ExecutableCommand implements Command, ScopeAware
{
    private Scope scope;
    private String targets;

    private void checkExe()
    {
        if (getExe() == null)
        {
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
                if (SystemUtils.isWindows())
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

    public void execute(File baseDir, File outputDir, CommandResult cmdResult)
    {
        checkExe();

        if (targets != null)
        {
            addArguments(targets.split(" +"));
            cmdResult.getProperties().put("targets", targets);
        }

        super.execute(baseDir, outputDir, cmdResult);
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
