package com.zutubi.pulse.cleanup.config;

import com.zutubi.prototype.ColumnFormatter;
import com.zutubi.i18n.Messages;

/**
 *
 *
 */
public class CleanupWhatColumnFormatter implements ColumnFormatter
{
    public String format(Object obj)
    {
        CleanupWhat what = (CleanupWhat) obj;
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

