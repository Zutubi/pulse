package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.validation.annotations.Numeric;

/**
 * Generic build options that don't warrant their own category.
 */
@SymbolicName("zutubi.buildOptionsConfig")
public class BuildOptionsConfiguration extends AbstractConfiguration
{
    public static final int TIMEOUT_NEVER = 0;

    private boolean isolateChangelists = false;
    private boolean retainWorkingCopy = false;
    @Numeric(min = 0)
    private int timeout = TIMEOUT_NEVER;
    private boolean prompt = false;

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
}
