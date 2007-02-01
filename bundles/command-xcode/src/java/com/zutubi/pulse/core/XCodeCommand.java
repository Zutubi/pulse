package com.zutubi.pulse.core;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.util.StringUtils;

import java.util.List;

/**
 * <class-comment/>
 */
public class XCodeCommand extends ExecutableCommand
{
    private String target;
    private String config;
    private String project;

    private String buildaction;
    private List<String> settings;

    private void checkExe()
    {
        if (getExe() == null)
        {
            Scope scope = getScope();
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

    public void execute(CommandContext context, CommandResult cmdResult)
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

    public void setScope(Scope scope)
    {
        super.setScope(scope);
        checkExe();
    }
}
