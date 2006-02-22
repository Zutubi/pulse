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

        if (cmdResult.succeeded())
        {
            // Unfortunately the ant.bat file on windows does not exit with
            // a non-zero code on failure.  Thus, we need to check the output
            // to see if ant is reporting failure.
            //
            // Even worse: ant gives different failure messages in different
            // cases, for example:
            //
            // jsankey@shiny:~/svn/bob/trunk$ ant -f nosuchfile
            // Buildfile: nosuchfile does not exist!
            // Build failed
            //
            // versus:
            //
            // jsankey@shiny:~/svn/bob/trunk$ ant nosuchtarget
            // Buildfile: build.xml
            //
            // BUILD FAILED
            // Target `nosuchtarget' does not exist in this project.
            //
            // Total time: 0 seconds
            RegexPostProcessor pp = new RegexPostProcessor("ant.pp");
            RegexPattern pattern = pp.createPattern();
            try
            {
                pattern.setExpression("^Build failed|^BUILD FAILED");
                pattern.setCategory("error");
                pp.process(outputDir, cmdResult.getArtifact("output"), cmdResult);
            }
            catch (FileLoadException e)
            {
                // Programmer error (RE not valid)
                e.printStackTrace();
            }
        }
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
