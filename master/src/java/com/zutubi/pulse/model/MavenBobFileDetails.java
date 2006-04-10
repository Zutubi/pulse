package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import org.apache.velocity.VelocityContext;

import java.util.Properties;

/**
 * <class-comment/>
 */
public class MavenBobFileDetails extends TemplateBobFileDetails
{
    /**
     * Space-separated list of target names (persists more efficiently)
     */
    private String targets;

    private String workingDir;

    private String arguments;

    protected String getTemplateName()
    {
        return "maven.template.vm";
    }

    protected void populateContext(VelocityContext context)
    {
        if (TextUtils.stringSet(targets))
        {
            String[] arg = targets.split(" ");
            if (arg.length > 0)
            {
                context.put("targets", arg);
            }
        }
        if (TextUtils.stringSet(workingDir))
        {
            context.put("workDir", workingDir.trim());
        }

        if (TextUtils.stringSet(arguments))
        {
            context.put("arguments", arguments);
        }

    }

    public String getType()
    {
        return "maven";
    }

    public Properties getProperties()
    {
        // TODO i18n
        Properties result = new Properties();

        if (TextUtils.stringSet(targets))
        {
            result.put("targets", targets);
        }

        if (TextUtils.stringSet(workingDir))
        {
            result.put("working directory", workingDir);
        }

        if (TextUtils.stringSet(arguments))
        {
            result.put("arguments", arguments);
        }

        return result;
    }

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
    }

    public String getWorkingDir()
    {
        return workingDir;
    }

    public void setWorkingDir(String workingDir)
    {
        this.workingDir = workingDir;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments(String arguments)
    {
        this.arguments = arguments;
    }
}
