package com.cinnamonbob.model;

import org.apache.velocity.VelocityContext;

import java.util.Map;
import java.util.TreeMap;

/**
 */
public class AntBobFileDetails extends TemplateBobFileDetails
{
    private String buildFile;
    /**
     * Space-separated list of target names (persists more efficiently)
     */
    private String targets;
    private Map<String, String> environment;

    public AntBobFileDetails()
    {
        buildFile = null;
        targets = null;
        environment = new TreeMap<String, String>();
    }

    public AntBobFileDetails(String buildFile, String targets, Map<String, String> environment)
    {
        this.buildFile = buildFile;
        this.targets = targets;
        this.environment = environment;
    }

    protected String getTemplateName()
    {
        return "ant.template.vm";
    }

    protected void populateContext(VelocityContext context)
    {
        if (buildFile != null)
        {
            context.put("buildFile", buildFile);
        }

        if (targets != null)
        {
            context.put("targets", targets.split(" "));
        }

        context.put("environment", environment);
    }

    public String getBuildFile()
    {
        return buildFile;
    }

    public void setBuildFile(String buildFile)
    {
        this.buildFile = buildFile;
    }

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
    }

    public Map<String, String> getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment)
    {
        this.environment = environment;
    }

    public void addEnvironmentalVariable(String name, String value)
    {
        environment.put(name, value);
    }
}
