package com.cinnamonbob.model;

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

    private String baseDir;

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
        if (TextUtils.stringSet(baseDir))
        {
            context.put("baseDir", baseDir.trim());
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

        if (TextUtils.stringSet(baseDir))
        {
            result.put("baseDir", baseDir);
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

    public String getBaseDir()
    {
        return baseDir;
    }

    public void setBaseDir(String baseDir)
    {
        this.baseDir = baseDir;
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
