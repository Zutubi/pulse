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
     * Extra arguments to pass to Ant.
     */
    private String arguments;
    /**
     * Path relative to base.dir in which to execute Ant.
     */
    private String workingDir;

    private Map<String, String> environment = new TreeMap<String, String>();

    public AntPulseFileDetails()
    {
    }

    public AntPulseFileDetails(String buildFile, String targets, String arguments, String workingDir, Map<String, String> environment)
    {
        this.buildFile = buildFile;
        this.targets = targets;
        this.arguments = arguments;
        this.workingDir = workingDir;
        if (environment != null)
        {
            this.environment.putAll(environment);
        }
    }

    public AntPulseFileDetails copy()
    {
        Map<String, String> env = new TreeMap<String, String>(environment);
        AntPulseFileDetails copy = new AntPulseFileDetails(buildFile, targets, arguments, workingDir, env);
        copyCommon(copy);
        return copy;
    }

    protected String getTemplateName()
    {
        return "ant.template.vm";
    }

    protected void populateContext(VelocityContext context)
    {
        if (TextUtils.stringSet(buildFile))
        {
            context.put("buildFile", buildFile);
        }

        if (TextUtils.stringSet(targets))
        {
            context.put("targets", targets);
        }

        if (TextUtils.stringSet(arguments))
        {
            context.put("arguments", arguments);
        }

        if (TextUtils.stringSet(workingDir))
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
        if (TextUtils.stringSet(env))
        {
            result.put("environment", env);
        }

        return result;
    }

    private String getEnvironmentString()
    {
        StringBuilder result = new StringBuilder();
        String sep = "";
        for (Map.Entry entry : environment.entrySet())
        {
            result.append(sep);
            result.append(entry.getKey());
            result.append('=');
            result.append(entry.getValue());
            sep = "; ";
        }

        return result.toString();
    }
}
