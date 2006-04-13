/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

import java.io.File;

/**
 */
public class MakeCommand extends ExecutableCommand implements Command, ScopeAware
{
    private Scope scope;
    private String makefile;
    private String targets;

    private void checkExe()
    {
        if (getExe() == null)
        {
            if (scope != null)
            {
                Reference ref = scope.getReference("make.bin");
                if (ref != null && ref.getValue() instanceof String)
                {
                    setExe((String) ref.getValue());
                }
            }

            if (getExe() == null)
            {
                setExe("make");
            }
        }
    }

    public void execute(File baseDir, File outputDir, CommandResult cmdResult)
    {
        checkExe();

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

        super.execute(baseDir, outputDir, cmdResult);
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

    public void setScope(Scope scope)
    {
        this.scope = scope;
        checkExe();
    }

}
