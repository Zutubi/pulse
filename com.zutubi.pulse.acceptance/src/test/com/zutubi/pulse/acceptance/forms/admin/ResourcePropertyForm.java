package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;

/**
 * Resource property form (suits wizard too).
 */
public class ResourcePropertyForm extends ConfigurationForm
{
    public ResourcePropertyForm(SeleniumBrowser browser)
    {
        super(browser, ResourcePropertyConfiguration.class, true, false);
    }

    public int[] getFieldTypes()
    {
        return new int[]{TEXTFIELD, TEXTFIELD, TEXTFIELD, CHECKBOX, CHECKBOX};
    }
}
