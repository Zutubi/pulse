package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.i18n.Messages;

/**
 * Formats table columns for trigger conditions.
 */
public class TriggerConditionConfigurationFormatter
{
    public String getType(TriggerConditionConfiguration config)
    {
        Messages i18n = Messages.getInstance(config.getClass());
        return i18n.format("condition.type");
    }
}
