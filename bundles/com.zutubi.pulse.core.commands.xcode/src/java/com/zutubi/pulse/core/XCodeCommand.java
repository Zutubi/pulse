package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.core.ExecutableCommand;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;

import java.util.List;

/**
 */
public class XCodeCommand extends ExecutableCommand
{
    private String target;
    private String config;
    private String project;

    private String buildaction;
    private List<String> settings;

    public XCodeCommand()
    {
        super("xcode.bin", "xcodebuild");
    }

    public void execute(ExecutionContext context, CommandResult cmdResult)
    {
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

        if (settings != null && settings.size() > 0)
        {
            addArguments(settings.toArray(new String[settings.size()]));
            cmdResult.getProperties().put("settings", StringUtils.unsplit(settings));
        }

        super.execute(context, cmdResult);

        StoredArtifact artifact = cmdResult.getArtifact(OUTPUT_ARTIFACT_NAME);
        if(artifact != null)
        {
            XCodePostProcessor pp = new XCodePostProcessor("xcode.pp");
            pp.process(artifact.getFile(), cmdResult, context);
        }
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public String getConfig()
    {
        return config;
    }

    public void setConfig(String config)
    {
        this.config = config;
    }

    public String getProject()
    {
        return project;
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    public String getBuildaction()
    {
        return buildaction;
    }

    public void setBuildaction(String buildaction)
    {
        this.buildaction = buildaction;
    }

    public List<String> getSettings()
    {
        return settings;
    }

    public void setSettings(List<String> settings)
    {
        this.settings = settings;
    }
}
