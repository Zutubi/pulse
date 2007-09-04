package com.zutubi.pulse.acceptance.forms.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 * The Subversion SCM form.
 */
public class SubversionForm extends SeleniumForm
{
    public SubversionForm(Selenium selenium)
    {
        super(selenium);
    }

    public String getFormName()
    {
        return "com.zutubi.pulse.core.scm.svn.config.SvnConfiguration";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "url", "username", "password", "keyfile", "keyfilePassphrase", "externalMonitorPaths", "verifyExternals", "monitor", "checkoutScheme", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod" };
    }

    public int[] getFieldTypes()
    {
        return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, CHECKBOX, CHECKBOX, COMBOBOX, CHECKBOX, TEXTFIELD, CHECKBOX, TEXTFIELD};
    }
}