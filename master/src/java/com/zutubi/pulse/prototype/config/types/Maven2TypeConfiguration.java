package com.zutubi.pulse.prototype.config.types;

import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("internal.maven2TypeConfig")
public class Maven2TypeConfiguration extends AbstractConfiguration
{
    private String goals;
    private String workingDir;
    private String arguments;

    public String getGoals()
    {
        return goals;
    }

    public void setGoals(String goals)
    {
        this.goals = goals;
    }

    public String getWorkingDir()
    {
        return workingDir;
    }

    public void setWorkingDir(String workingDir)
    {
        this.workingDir = workingDir;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments(String arguments)
    {
        this.arguments = arguments;
    }
}
