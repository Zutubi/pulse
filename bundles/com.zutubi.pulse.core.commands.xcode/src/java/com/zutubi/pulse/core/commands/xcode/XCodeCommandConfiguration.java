package com.zutubi.pulse.core.commands.xcode;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.StringList;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for instances of {@link XCodeCommand}.
 */
@SymbolicName("zutubi.xcodeCommandConfig")
@Form(fieldOrder = {"name", "workingDir", "project", "config", "target", "buildaction", "settings", "args", "extraArguments", "postProcessors", "exe", "inputFile", "outputFile", "force"})
public class XCodeCommandConfiguration extends NamedArgumentCommandConfiguration
{
    private String target;
    private String config;
    private String project;
    private String buildaction;
    @StringList
    private List<String> settings;

    public XCodeCommandConfiguration()
    {
        super(XCodeCommand.class, "xcode.bin", "xcodebuild");
    }

    protected List<NamedArgument> getNamedArguments()
    {
        List<NamedArgument> result = new LinkedList<NamedArgument>();
        if (StringUtils.stringSet(project))
        {
            result.add(new NamedArgument("project", project, "-project"));
        }

        if (StringUtils.stringSet(config))
        {
            result.add(new NamedArgument("configuration", config, "-configuration"));
        }

        if (StringUtils.stringSet(target))
        {
            result.add(new NamedArgument("target", target, "-target"));
        }

        if (StringUtils.stringSet(buildaction))
        {
            result.add(new NamedArgument("build action", buildaction));
        }

        if (settings != null && settings.size() > 0)
        {
            result.add(new NamedArgument("settings", StringUtils.unsplit(settings), settings));
        }

        return result;
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
