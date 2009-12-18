package com.zutubi.pulse.acceptance.pages;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

/**
 * Simple OK/Cancel dialog.
 */
public class ConfirmDialog extends MessageDialog
{
    public ConfirmDialog(SeleniumBrowser browser)
    {
        super(browser, false);
    }
}
