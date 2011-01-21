package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * A page representing the admin settings license page.
 */
public class LicensePage extends CompositePage
{
    private static final String LICENSE_PATH = "settings/license";

    private static final String SUPPORT_EXPIRY_FIELD = "supportExpiry";
    private static final String EXPIRY_FIELD = "expiry";
    private static final String NAME_FIELD = "name";

    public LicensePage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, LICENSE_PATH);
    }

    public String getName()
    {
        return getStateField(NAME_FIELD);
    }

    public boolean isNamePresent()
    {
        return isStateFieldPresent(NAME_FIELD);
    }

    public String getExpiry()
    {
        return getStateField(EXPIRY_FIELD);
    }

    public boolean isExpiryPresent()
    {
        return isStateFieldPresent(EXPIRY_FIELD);
    }

    public String getSupportExpiry()
    {
        return getStateField(SUPPORT_EXPIRY_FIELD);
    }

    public boolean isSupportExpiryPresent()
    {
        return isStateFieldPresent(SUPPORT_EXPIRY_FIELD);
    }
}
