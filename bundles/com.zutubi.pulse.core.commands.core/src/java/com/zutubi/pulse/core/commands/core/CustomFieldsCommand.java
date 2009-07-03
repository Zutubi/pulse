package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.CommandSupport;

/**
 * A command that adds custom fields to the recipe or build result.
 */
public class CustomFieldsCommand extends CommandSupport
{
    public CustomFieldsCommand(CustomFieldsCommandConfiguration config)
    {
        super(config);
    }

    public void execute(CommandContext commandContext)
    {
        CustomFieldsCommandConfiguration config = (CustomFieldsCommandConfiguration) getConfig();
        for (CustomFieldConfiguration field: config.getFields().values())
        {
            commandContext.addCustomField(field.getScope(), field.getName(), field.getValue());
        }
    }
}
