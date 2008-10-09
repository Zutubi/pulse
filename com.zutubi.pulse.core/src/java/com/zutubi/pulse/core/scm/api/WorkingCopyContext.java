package com.zutubi.pulse.core.scm.api;

import com.zutubi.util.config.Config;

import java.io.File;

/**
 * The context in which working copy operations are run.
 */
public interface WorkingCopyContext
{
    File getBase();
    Config getConfig();
}
