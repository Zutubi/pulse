package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
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
    private Map<String, String> environment = new TreeMap<String, String>();

    public MakePulseFileDetails()
    {
        makefile = null;
        targets = null;
        arguments = null;
        workingDir = null;
    }

    public MakePulseFileDetails(String buildFile, String targets, String arguments, String workingDir)
    {
        this.makefile = buildFile;
        this.targets = targets;
        this.arguments = arguments;
        this.workingDir = workingDir;
    }

    public MakePulseFileDetails copy()
    {
        MakePulseFileDetails copy = new MakePulseFileDetails(makefile, targets, arguments, workingDir);
        copy.environment = new TreeMap<String, String>(environment);
        copyCommon(copy);
        return copy;
    }

    protected String getTemplateName()
    {
        return "make.template.vm";
    }

    protected void populateContext(VelocityContext context)
    {
        if (TextUtils.stringSet(makefile))
        {
            context.put("makefile", makefile);
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

        if (TextUtils.stringSet(makefile))
        {
            result.put("makefile", makefile);
        }

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
