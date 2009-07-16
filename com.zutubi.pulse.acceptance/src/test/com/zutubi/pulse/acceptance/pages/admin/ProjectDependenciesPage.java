package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.forms.admin.DependencyForm;
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

    public DependencyForm clickAdd()
    {
        browser.click(ListPage.ADD_LINK);
        return browser.createForm(DependencyForm.class);
    }
}
