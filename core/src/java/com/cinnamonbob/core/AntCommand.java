package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.util.SystemUtils;

import java.io.File;

/**
 */
public class AntCommand extends ExecutableCommand implements Command, ScopeAware
{
    private Scope scope;
    private String buildFile;
    private String targets;

    private void checkExe()
    {
        if (getExe() == null)
        {
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

    public void execute(File baseDir, File outputDir, CommandResult cmdResult)
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

        super.execute(baseDir, outputDir, cmdResult);

        AntPostProcessor pp = new AntPostProcessor("ant.pp");
        pp.process(outputDir, cmdResult.getArtifact(OUTPUT_NAME).getFile(), cmdResult);
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
        this.scope = scope;
        checkExe();
    }
}
