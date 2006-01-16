package com.cinnamonbob.model;

import org.apache.velocity.VelocityContext;
import com.opensymphony.util.TextUtils;

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
    }

    public String getType()
    {
        return "maven";
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
}
