package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import org.apache.velocity.VelocityContext;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 */
public class AntPulseFileDetails extends TemplatePulseFileDetails
{
    private String buildFile;
    /**
     * Space-separated list of target names (persists more efficiently)
     */
    private String targets;
    /**
     * Extra arguments to pass to make.
     */
    private String arguments;
    /**
     * Path relative to work.dir in which to execute the make.
     */
    private String workingDir;
    private Map<String, String> environment;

    public AntPulseFileDetails()
    {
        buildFile = null;
        targets = null;
        arguments = null;
        workingDir = null;
        environment = new TreeMap<String, String>();
    }

    public AntPulseFileDetails(String buildFile, String targets, String arguments, String workingDir, Map<String, String> environment)
    {
        this.buildFile = buildFile;
        this.targets = targets;
        this.arguments = arguments;
        this.workingDir = workingDir;
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
            context.put("targets", targets);
        }

        if (arguments != null)
        {
            context.put("arguments", arguments);
        }

        if (workingDir != null)
        {
            context.put("workingDir", workingDir);
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

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments(String arguments)
    {
        this.arguments = arguments;
    }

    public String getWorkingDir()
    {
        return workingDir;
    }

    public void setWorkingDir(String workingDir)
    {
        this.workingDir = workingDir;
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

    public String getType()
    {
        return "ant";
    }

    public Properties getProperties()
    {
        // TODO i18n
        Properties result = new Properties();

        if (TextUtils.stringSet(buildFile))
        {
            result.put("build file", buildFile);
        }

        if (TextUtils.stringSet(targets))
        {
            result.put("targets", targets);
        }

        if (TextUtils.stringSet(arguments))
        {
            result.put("arguments", arguments);
        }

        if (TextUtils.stringSet(workingDir))
        {
            result.put("working directory", workingDir);
        }

        String env = getEnvironmentString();
        if (env.length() > 0)
        {
            result.put("environment", env);
        }

        return result;
    }

    private String getEnvironmentString()
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry entry : environment.entrySet())
        {
            if (!first)
            {
                result.append("; ");
                first = false;
            }

            result.append(entry.getKey());
            result.append('=');
            result.append(entry.getValue());
        }

        return result.toString();
    }
}
