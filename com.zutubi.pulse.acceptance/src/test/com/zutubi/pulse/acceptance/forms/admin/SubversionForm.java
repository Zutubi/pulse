package com.zutubi.pulse.acceptance.forms.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.SeleniumForm;

/**
 * The Subversion SCM form.
 */
public class SubversionForm extends SeleniumForm
{
    public SubversionForm(SeleniumBrowser browser)
    {
        super(browser);
    }

    public String getFormName()
    {
        return "com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration";
    }

    public String[] getFieldNames()
    {
        return new String[]{ "url", "username", "password", "keyfile", "keyfilePassphrase", "externalsMonitoring", "externalMonitorPaths", "verifyExternals", "monitor", "checkoutScheme", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod" };
    }

    public int[] getFieldTypes()
    {
        return new int[]{ TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, TEXTFIELD, COMBOBOX, ITEM_PICKER, CHECKBOX, CHECKBOX, COMBOBOX, CHECKBOX, TEXTFIELD, CHECKBOX, TEXTFIELD};
    }
}