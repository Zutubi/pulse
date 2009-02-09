package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.tove.config.annotations.BrowseScmDirAction;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.TextUtils;
import org.apache.velocity.VelocityContext;

/**
 *
 *
 */
@SymbolicName("zutubi.xcodeTypeConfig")
@Form(fieldOrder = {"workingDir", "project", "configuration", "target", "action", "settings", "postProcessors"})
public class XCodeTypeConfiguration extends TemplateTypeConfiguration
{
    @BrowseScmDirAction
    private String workingDir = null;
    private String config = null;
    private String project = null;
    private String target = null;
    private String action = null;
    private String settings = null;

    public String getWorkingDir()
    {
        return workingDir;
    }

    public void setWorkingDir(String workingDir)
    {
        this.workingDir = workingDir;
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

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getSettings()
    {
        return settings;
    }

    public void setSettings(String settings)
    {
        this.settings = settings;
    }

    protected String getTemplateName()
    {
        return "xcode.template.vm";
    }

    protected void setupContext(VelocityContext context)
    {
        if (TextUtils.stringSet(workingDir))
        {
            context.put("workingDir", workingDir);
        }
        if (TextUtils.stringSet(config))
        {
            context.put("config", config);
        }
        if (TextUtils.stringSet(project))
        {
            context.put("project", project);
        }
        if (TextUtils.stringSet(target))
        {
            context.put("target", target);
        }
        if (TextUtils.stringSet(action))
        {
            context.put("action", action);
        }
        if (TextUtils.stringSet(settings))
        {
            context.put("settings", settings);
        }
    }
}
