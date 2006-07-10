package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.opensymphony.util.TextUtils;

import java.io.File;

/**
 * <class-comment/>
 */
public class XCodeCommand extends ExecutableCommand
{
    private Scope scope;

    private String target;
    private String config;
    private String project;

    private String buildaction;
    private String settings;

    private void checkExe()
    {
        if (getExe() == null)
        {
            if (scope != null)
            {
                Reference ref = scope.getReference("xcode.bin");
                if (ref != null && ref.getValue() instanceof String)
                {
                    setExe((String) ref.getValue());
                }
            }

            if (getExe() == null)
            {
                setExe("xcodebuild");
            }
        }
    }

    public void execute(long recipeId, RecipePaths paths, File outputDir, CommandResult cmdResult)
    {
        checkExe();

        if (TextUtils.stringSet(project))
        {
            addArguments("-project", project);
            cmdResult.getProperties().put("project", project);
        }

        if (TextUtils.stringSet(config))
        {
            addArguments("-configuration", config);
            cmdResult.getProperties().put("configuration", config);
        }

        if (TextUtils.stringSet(target))
        {
            addArguments("-target", target);
            cmdResult.getProperties().put("target", target);
        }

        if (TextUtils.stringSet(buildaction))
        {
            addArguments(buildaction);
            cmdResult.getProperties().put("build action", buildaction);
        }

        if (TextUtils.stringSet(settings))
        {
            addArguments(settings);
            cmdResult.getProperties().put("settings", settings);
        }

        super.execute(recipeId, paths, outputDir, cmdResult);

        XCodePostProcessor pp = new XCodePostProcessor("xcode.pp");
        pp.process(outputDir, cmdResult.getArtifact(OUTPUT_NAME).getFile(), cmdResult);
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public void setConfig(String config)
    {
        this.config = config;
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    public void setBuildaction(String buildaction)
    {
        this.buildaction = buildaction;
    }

    public void setSettings(String settings)
    {
        this.settings = settings;
    }

    public void setScope(Scope scope)
    {
        this.scope = scope;
        checkExe();
    }
}
