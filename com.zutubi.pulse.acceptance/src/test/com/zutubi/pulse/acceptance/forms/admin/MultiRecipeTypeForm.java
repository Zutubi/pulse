package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;

/**
 * Form for configuration multiple-recipe projects.
 */
public class MultiRecipeTypeForm extends ConfigurationForm
{
    public MultiRecipeTypeForm(SeleniumBrowser browser)
    {
        super(browser, MultiRecipeTypeConfiguration.class);
    }
}
