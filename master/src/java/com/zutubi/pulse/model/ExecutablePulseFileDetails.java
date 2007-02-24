package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import org.apache.velocity.VelocityContext;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 */
public class ExecutablePulseFileDetails extends TemplatePulseFileDetails
{
    /**
     * The command to execute.
     */
    private String executable;
    /**
     * Space-separated list of arguments to pass (StringUtils.split format).
     */
    private String arguments;
    /**
     * Path relative to base.dir in which to execute the make.
     */
    private String workingDir;

    private Map<String, String> environment = new TreeMap<String, String>();

    public ExecutablePulseFileDetails()
    {
    }

    public ExecutablePulseFileDetails(String executable, String arguments, String workingDir, Map<String, String> environment)
    {
        this.executable = executable;
        this.arguments = arguments;
        this.workingDir = workingDir;
        if (environment != null)
        {
            this.environment.putAll(environment);
        }
    }

    public ExecutablePulseFileDetails copy()
    {
        Map<String, String> env = new TreeMap<String, String>(environment);
        ExecutablePulseFileDetails copy = new ExecutablePulseFileDetails(executable, arguments, workingDir, env);
        copyCommon(copy);
        return copy;
    }

    protected String getTemplateName()
    {
        return "executable.template.vm";
    }

    protected void populateContext(VelocityContext context)
    {
        if (TextUtils.stringSet(executable))
        {
            context.put("executable", executable);
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

    public String getExecutable()
    {
        return executable;
    }

    public void setExecutable(String executable)
    {
        this.executable = executable;
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
        return "executable";
    }

    public Properties getProperties()
    {
        // TODO i18n
        Properties result = new Properties();

        if (TextUtils.stringSet(executable))
        {
            result.put("executable", executable);
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
