package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Defines how build stages for a project are bootstrapped.
 */
@SymbolicName("zutubi.bootstrapConfig")
@Form(fieldOrder = {"checkoutScheme", "persistentDirPattern", "tempDirPattern"})
public class BootstrapConfiguration extends AbstractConfiguration
{
    private CheckoutScheme checkoutScheme = CheckoutScheme.CLEAN_CHECKOUT;
    @Required
    private String persistentDirPattern = "$(agent.data.dir)/work/$(project.handle)/$(stage.handle)";
    @Required
    private String tempDirPattern = "$(agent.data.dir)/recipes/$(recipe.id)/base";

    public CheckoutScheme getCheckoutScheme()
    {
        return checkoutScheme;
    }

    public void setCheckoutScheme(CheckoutScheme checkoutScheme)
    {
        this.checkoutScheme = checkoutScheme;
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
