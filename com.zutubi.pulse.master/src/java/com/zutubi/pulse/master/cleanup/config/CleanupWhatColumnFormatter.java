package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.i18n.Messages;
import com.zutubi.tove.config.api.Formatter;

/**
 * An implementation of the Formatter interface that handles the formatting
 * of CleanupWhat instances.
 *
 * @see com.zutubi.pulse.master.cleanup.config.CleanupWhat
 */
public class CleanupWhatColumnFormatter implements Formatter<CleanupWhat>
{
    private static final Messages I18N = Messages.getInstance(CleanupConfiguration.class);
    
    public String format(CleanupWhat what)
    {
        String key = "what." + what + ".label";
        if (I18N.isKeyDefined(key))
        {
            return I18N.format(key);
        }
        return key;
    }
}

