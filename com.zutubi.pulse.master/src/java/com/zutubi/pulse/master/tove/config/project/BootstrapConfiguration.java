package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Defines how build stages for a project are bootstrapped.
 */
@SymbolicName("zutubi.bootstrapConfig")
@Form(fieldOrder = {"checkoutType", "buildType", "persistentDirPattern", "tempDirPattern"})
public class BootstrapConfiguration extends AbstractConfiguration
{
    @Required
    private CheckoutType checkoutType = CheckoutType.CLEAN_CHECKOUT;
    @Required
    private BuildType buildType = BuildType.CLEAN_BUILD;
    @Required
    private String persistentDirPattern = "$(agent.data.dir)/work/$(project.handle)/$(stage.handle)";
    @Required
    private String tempDirPattern = "$(agent.data.dir)/recipes/$(recipe.id)/base";

    public CheckoutType getCheckoutType()
    {
        return checkoutType;
    }

    public void setCheckoutType(CheckoutType checkoutType)
    {
        this.checkoutType = checkoutType;
    }

    public BuildType getBuildType()
    {
        return buildType;
    }

    public void setBuildType(BuildType buildType)
    {
        this.buildType = buildType;
    }

    public String getPersistentDirPattern()
    {
        return persistentDirPattern;
    }

    public void setPersistentDirPattern(String persistentDirPattern)
    {
        this.persistentDirPattern = persistentDirPattern;
    }

    public String getTempDirPattern()
    {
        return tempDirPattern;
    }

    public void setTempDirPattern(String tempDirPattern)
    {
        this.tempDirPattern = tempDirPattern;
    }
}
