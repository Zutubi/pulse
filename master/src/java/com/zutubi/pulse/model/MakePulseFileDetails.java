package com.zutubi.pulse.model;

import org.apache.velocity.VelocityContext;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 */
public class MakePulseFileDetails extends TemplatePulseFileDetails
{
    private String makefile;
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

    public MakePulseFileDetails()
    {
        makefile = null;
        targets = null;
        arguments = null;
        workingDir = null;
        environment = new TreeMap<String, String>();
    }

    public MakePulseFileDetails(String buildFile, String targets, String arguments, String workingDir, Map<String, String> environment)
    {
        this.makefile = buildFile;
        this.targets = targets;
        this.arguments = arguments;
        this.workingDir = workingDir;
        this.environment = environment;
    }

    public MakePulseFileDetails copy()
    {
        Map<String, String> env = new TreeMap<String, String>(environment);
        return new MakePulseFileDetails(makefile, targets, arguments, workingDir, env);
    }

    protected String getTemplateName()
    {
        return "make.template.vm";
    }

    protected void populateContext(VelocityContext context)
    {
        if (makefile != null)
        {
            context.put("makefile", makefile);
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

    public String getMakefile()
    {
        return makefile;
    }

    public void setMakefile(String makefile)
    {
        this.makefile = makefile;
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
        return "make";
    }

    public Properties getProperties()
    {
        // TODO i18n
        Properties result = new Properties();

        if (makefile != null)
        {
            result.put("makefile", makefile);
        }

        if (targets != null)
        {
            result.put("targets", targets);
        }

        if (workingDir != null)
        {
            result.put("working directory", workingDir);
        }

        if (arguments != null)
        {
            result.put("arguments", arguments);
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
