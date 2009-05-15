package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The list page representing the list of currently configured cleanup rules
 * for a specific project.
 */
public class CleanupRulesPage extends ListPage
{
    public CleanupRulesPage(Selenium selenium, Urls urls, String path)
    {
        super(selenium, urls, path);
    }
}
