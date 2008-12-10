package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.i18n.Messages;
import com.zutubi.tove.annotations.api.Formatter;

/**
 *
 *
 */
public class CleanupWhatColumnFormatter implements Formatter<CleanupWhat>
{
    public String format(CleanupWhat what)
    {
        if (what == CleanupWhat.WHOLE_BUILDS)
        {
            return Messages.format(CleanupConfiguration.class, "what.WHOLE_BUILDS.label");
        }
        else if (what == CleanupWhat.WORKING_DIRECTORIES_ONLY)
        {
            return Messages.format(CleanupConfiguration.class, "what.WORKING_DIRECTORIES_ONLY.label");
        }
        return "";
    }
}

