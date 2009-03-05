package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;

/**
 * Form for configuration multiple-recipe projects.
 */
public class MultiRecipeTypeForm extends ConfigurationForm
{
    public MultiRecipeTypeForm(Selenium selenium)
    {
        super(selenium, MultiRecipeTypeConfiguration.class);
    }
}
