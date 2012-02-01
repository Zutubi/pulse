package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * A subscription to results for personal builds.
 */
@SymbolicName("zutubi.personalSubscriptionConfig")
@Form(fieldOrder = {"name", "contact", "template", "attachLogs", "logLineLimit"})
@Classification(single = "favourite")
@Wire
public class PersonalSubscriptionConfiguration extends SubscriptionConfiguration
{
    private ConfigurationProvider configurationProvider;

    public boolean conditionSatisfied(BuildResult buildResult)
    {
        return buildResult.isPersonal() && buildResult.getUser().getConfig().equals(configurationProvider.getAncestorOfType(this, UserConfiguration.class));
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
