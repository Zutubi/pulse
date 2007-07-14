package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Select;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.model.BuildResult;

/**
 * A subscription to results for personal builds.
 */
@SymbolicName("zutubi.personalSubscriptionConfig")
@Form(fieldOrder = {"name", "contact", "template"})
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
