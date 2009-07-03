package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for custom-fields.
 */
public class CustomFieldsCommandConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        CustomFieldsCommandConfiguration command = new CustomFieldsCommandConfiguration("fields");
        command.addField(new CustomFieldConfiguration("codename", "foxtrot", FieldScope.BUILD));
        return ExamplesBuilder.buildProject(command);
    }
}
