package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.i18n.Messages;
import com.zutubi.util.StringUtils;

public class CleanupConfigurationFormatter
{
    private static final Messages I18N = Messages.getInstance(CleanupConfiguration.class);

    private CleanupWhatColumnFormatter whatFormatter = new CleanupWhatColumnFormatter();

    public String getWhat(CleanupConfiguration config)
    {
        if (config.isCleanupAll())
        {
            return I18N.format("what.ALL.label");
        }
        else
        {
            StringBuffer buffer = new StringBuffer();
            String sep = "";
            for (CleanupWhat what : config.getWhat())
            {
                buffer.append(sep);
                buffer.append(whatFormatter.format(what));
                sep = ", ";
            }
            return StringUtils.trimmedString(buffer, 40);
        }
    }

    public String getAfter(CleanupConfiguration config)
    {
        if (config.getRetain() < 1)
        {
            return "never";
        }

        return I18N.format("retain." + config.getUnit().toString(), config.getRetain());
    }
}
