package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.MessageDialog;

/**
 * Represents the popup prompt show when taking responsibility for a build.
 */
public class TakeResponsibilityDialog extends MessageDialog
{
    public TakeResponsibilityDialog(SeleniumBrowser browser)
    {
        super(browser, false);
    }
}
