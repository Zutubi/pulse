package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * A subscription to results for personal builds.
 */
@SymbolicName("zutubi.personalSubscriptionConfig")
@Form(fieldOrder = {"name", "contact", "template"})
@Classification(single = "favourite")
@Wire
public class PersonalSubscriptionConfiguration extends SubscriptionConfiguration
{
    @Select(optionProvider = "SubscriptionTemplateOptionProvider")
    private String template;

    private ConfigurationProvider configurationProvider;

    public boolean conditionSatisfied(BuildResult buildResult)
    {
        return buildResult.isPersonal() && buildResult.getUser().getConfig().equals(configurationProvider.getAncestorOfType(this, UserConfiguration.class));
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
