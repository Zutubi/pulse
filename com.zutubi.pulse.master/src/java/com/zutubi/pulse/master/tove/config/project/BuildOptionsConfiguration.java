package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.Undefined;
import com.zutubi.validation.annotations.Numeric;

/**
 * Generic build options that don't warrant their own category.
 */
@SymbolicName("zutubi.buildOptionsConfig")
@Form(fieldOrder = {"isolateChangelists", "prompt", "timeout", "stageFailureLimit", "autoClearResponsibility", "idLeader", "persistentWorkDir", "logCompressionEnabled"})
public class BuildOptionsConfiguration extends AbstractConfiguration
{
    public static final int TIMEOUT_NEVER = 0;

    private boolean isolateChangelists = false;
    @Numeric(min = 0)
    private int timeout = TIMEOUT_NEVER;
    private boolean prompt = false;
    private int stageFailureLimit = 0;
    private boolean autoClearResponsibility = true;
    @Reference
    private ProjectConfiguration idLeader = null;
    private String persistentWorkDir = "$(agent.data.dir)/work/$(project.handle)/$(stage.handle)";
    private boolean logCompressionEnabled = true;

    private int priority = Undefined.INTEGER;

    public BuildOptionsConfiguration()
    {
        setPermanent(true);
    }

    public boolean getIsolateChangelists()
    {
        return isolateChangelists;
    }

    public void setIsolateChangelists(boolean b)
    {
        this.isolateChangelists = b;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public boolean getPrompt()
    {
        return prompt;
    }

    public void setPrompt(boolean b)
    {
        this.prompt = b;
    }

    public int getStageFailureLimit()
    {
        return stageFailureLimit;
    }

    public void setStageFailureLimit(int stageFailureLimit)
    {
        this.stageFailureLimit = stageFailureLimit;
    }

    public boolean isAutoClearResponsibility()
    {
        return autoClearResponsibility;
    }

    public void setAutoClearResponsibility(boolean autoClearResponsibility)
    {
        this.autoClearResponsibility = autoClearResponsibility;
    }

    public ProjectConfiguration getIdLeader()
    {
        return idLeader;
    }

    public void setIdLeader(ProjectConfiguration idLeader)
    {
        this.idLeader = idLeader;
    }

    public String getPersistentWorkDir()
    {
        return persistentWorkDir;
    }

    public void setPersistentWorkDir(String persistentWorkDir)
    {
        this.persistentWorkDir = persistentWorkDir;
    }

    public boolean isLogCompressionEnabled()
    {
        return logCompressionEnabled;
    }

    public void setLogCompressionEnabled(boolean logCompressionEnabled)
    {
        this.logCompressionEnabled = logCompressionEnabled;
    }

    public boolean hasPriority()
    {
        return priority != Undefined.INTEGER;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }
}
