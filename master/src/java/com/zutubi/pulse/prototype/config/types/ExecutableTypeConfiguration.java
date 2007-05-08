package com.zutubi.pulse.prototype.config.types;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 *
 */
@SymbolicName("internal.executableTypeConfig")
public class ExecutableTypeConfiguration extends AbstractConfiguration
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

//    private Map<String, String> environment = new TreeMap<String, String>();

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

/*
    public Map<String, String> getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment)
    {
        this.environment = environment;
    }
*/
}
