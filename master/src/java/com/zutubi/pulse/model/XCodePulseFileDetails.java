package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import org.apache.velocity.VelocityContext;

import java.util.Properties;

/**
 * <class-comment/>
 */
public class XCodePulseFileDetails  extends TemplatePulseFileDetails
{
    private String workingDir = null;
    private String config = null;
    private String project = null;
    private String target = null;
    private String action = null;
    private String settings = null;

    public PulseFileDetails copy()
    {
        XCodePulseFileDetails copy = new XCodePulseFileDetails();
        copy.workingDir = workingDir;
        copy.config = config;
        copy.project = project;
        copy.target = target;
        copy.action = action;
        copy.settings = settings;
        copyCommon(copy);
        return copy;
    }

    protected String getTemplateName()
    {
        return "xcode.template.vm";
    }

    protected void populateContext(VelocityContext context)
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

    public String getType()
    {
        return "xcode";
    }

    public Properties getProperties()
    {
        Properties props = new Properties();
        if (TextUtils.stringSet(workingDir))
        {
            props.put("working directory", workingDir);
        }
        if (TextUtils.stringSet(config))
        {
            props.put("configuration", config);
        }
        if (TextUtils.stringSet(project))
        {
            props.put("project", project);
        }
        if (TextUtils.stringSet(target))
        {
            props.put("target", target);
        }
        if (TextUtils.stringSet(action))
        {
            props.put("build action", action);
        }
        if (TextUtils.stringSet(settings))
        {
            props.put("settings", settings);
        }
        return props;
    }

    //---( simple property accessors )---

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

    public String getSettings()
    {
        return settings;
    }

    public void setSettings(String settings)
    {
        this.settings = settings;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }
}
