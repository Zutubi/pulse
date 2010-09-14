package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.util.config.Config;

import java.io.File;

/**
 * A simple implementation of {@link com.zutubi.pulse.core.scm.api.WorkingCopyContext}.
 */
public class WorkingCopyContextImpl implements WorkingCopyContext
{
    private File base;
    private Config config;
    private UserInterface ui;

    public WorkingCopyContextImpl(File base, Config config, UserInterface ui)
    {
        this.base = base;
        this.config = config;
        this.ui = ui;
    }

    public File getBase()
    {
        return base;
    }

    public Config getConfig()
    {
        return config;
    }

    public UserInterface getUI()
    {
        return ui;
    }
}
