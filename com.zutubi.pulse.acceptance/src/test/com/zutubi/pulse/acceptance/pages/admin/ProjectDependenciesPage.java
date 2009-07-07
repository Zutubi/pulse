package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The project dependencies page.
 */
public class ProjectDependenciesPage extends CompositePage
{
    public ProjectDependenciesPage(SeleniumBrowser browser, Urls urls, String path)
    {
        super(browser, urls, path);
    }

    public DependenciesPage clickDependenciesAndWait()
    {
        ListPage listPage = clickCollection("dependencies", "dependencies");
        return browser.waitFor(DependenciesPage.class, listPage.getPath());
    }
}
