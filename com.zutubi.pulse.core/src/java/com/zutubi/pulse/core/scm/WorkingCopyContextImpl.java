package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import com.zutubi.pulse.core.util.config.Config;

import java.io.File;

/**
 * A simple implementation of {@link com.zutubi.pulse.core.scm.api.WorkingCopyContext}.
 */
public class WorkingCopyContextImpl implements WorkingCopyContext
{
    private File base;
    private Config config;

    public WorkingCopyContextImpl(File base, Config config)
    {
        this.base = base;
        this.config = config;
    }

    public File getBase()
    {
        return base;
    }

    public Config getConfig()
    {
        return config;
    }
}
