package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.PersonalBuildUI;
import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import com.zutubi.util.config.Config;

import java.io.File;

/**
 * A simple implementation of {@link com.zutubi.pulse.core.scm.api.WorkingCopyContext}.
 */
public class WorkingCopyContextImpl implements WorkingCopyContext
{
    private File base;
    private Config config;
    private PersonalBuildUI ui;

    public WorkingCopyContextImpl(File base, Config config, PersonalBuildUI ui)
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

    public PersonalBuildUI getUI()
    {
        return ui;
    }
}
