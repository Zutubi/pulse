package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The list page representing the list of currently configured cleanup rules
 * for a specific project.
 */
public class CleanupRulesPage extends ListPage
{
    public CleanupRulesPage(SeleniumBrowser browser, Urls urls, String path)
    {
        super(browser, urls, path);
    }
}
