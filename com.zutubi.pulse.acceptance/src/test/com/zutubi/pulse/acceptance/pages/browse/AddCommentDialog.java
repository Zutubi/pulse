package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.MessageDialog;

/**
 * Represents the popup prompt shown when commenting on a build.
 */
public class AddCommentDialog extends MessageDialog
{
    public AddCommentDialog(SeleniumBrowser browser)
    {
        super(browser, true);
    }
}