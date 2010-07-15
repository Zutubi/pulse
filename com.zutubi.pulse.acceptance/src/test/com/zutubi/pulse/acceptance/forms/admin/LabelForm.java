package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;

/**
 * Project label form.
 */
public class LabelForm extends ConfigurationForm
{
    public LabelForm(SeleniumBrowser browser)
    {
        super(browser, LabelConfiguration.class);
    }

    public String[] getFieldNames()
    {
        return new String[]{"label"};
    }
}
