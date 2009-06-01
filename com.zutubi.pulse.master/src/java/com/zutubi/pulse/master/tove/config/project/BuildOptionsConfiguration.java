package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Numeric;

/**
 * Generic build options that don't warrant their own category.
 */
@SymbolicName("zutubi.buildOptionsConfig")
@Form(fieldOrder = {"isolateChangelists", "prompt", "retainWorkingCopy", "timeout", "autoClearResponsibility"})
public class BuildOptionsConfiguration extends AbstractConfiguration
{
    public static final int TIMEOUT_NEVER = 0;

    private boolean isolateChangelists = false;
    private boolean retainWorkingCopy = false;
    @Numeric(min = 0)
    private int timeout = TIMEOUT_NEVER;
    private boolean prompt = false;
    private boolean autoClearResponsibility = true;

    public boolean getIsolateChangelists()
    {
        return isolateChangelists;
    }

    public void setIsolateChangelists(boolean b)
    {
        this.isolateChangelists = b;
    }

    public boolean getRetainWorkingCopy()
    {
        return retainWorkingCopy;
    }

    public void setRetainWorkingCopy(boolean b)
    {
        this.retainWorkingCopy = b;
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

    public boolean isAutoClearResponsibility()
    {
        return autoClearResponsibility;
    }

    public void setAutoClearResponsibility(boolean autoClearResponsibility)
    {
        this.autoClearResponsibility = autoClearResponsibility;
    }
}
