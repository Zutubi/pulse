package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * Yes/no confirmation dialog.
 */
public class YesNoDialog extends MessageDialog
{
    public YesNoDialog(SeleniumBrowser browser)
    {
        super(browser, true, "yes", "no");
    }
}
