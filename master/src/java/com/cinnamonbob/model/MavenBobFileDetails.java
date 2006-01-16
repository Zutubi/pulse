package com.cinnamonbob.model;

import org.apache.velocity.VelocityContext;

/**
 * <class-comment/>
 */
public class MavenBobFileDetails extends TemplateBobFileDetails
{
    /**
     * Space-separated list of target names (persists more efficiently)
     */
    private String targets;

    protected String getTemplateName()
    {
        return "maven.template.vm";
    }

    protected void populateContext(VelocityContext context)
    {
        if (targets != null)
        {
            context.put("targets", targets.split(" "));
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
}
