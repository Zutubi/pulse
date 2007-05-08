package com.zutubi.pulse.prototype.config.types;

import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.config.annotations.SymbolicName;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 *
 */
@SymbolicName("internal.makeTypeConfig")
public class MakeTypeConfiguration extends AbstractConfiguration
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
    
//    private Map<String, String> environment = new TreeMap<String, String>();

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
