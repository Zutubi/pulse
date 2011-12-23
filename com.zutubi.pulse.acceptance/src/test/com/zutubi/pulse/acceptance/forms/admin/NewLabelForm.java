package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.master.tove.config.NewLabelConfiguration;

/**
 * Project label renaming form.
 */
public class NewLabelForm extends ConfigurationForm
{
    public NewLabelForm(SeleniumBrowser browser)
    {
        super(browser, NewLabelConfiguration.class);
    }

    public String[] getFieldNames()
    {
        return new String[]{"label"};
    }
}
