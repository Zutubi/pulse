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

    /**
     * Get the contents of the requested row from the dependencies table.  The row
     * index starts at 1 for the first row.
     *
     * @param row   the row index, starting at 1.
     *
     * @return the contents of the row in the form of an array.  Index 0 is the project name,
     * index 1 is the revision, index 2 is the stage list, index 3 is the transitive field.
     */
    public String[] getDependencyRow(int row)
    {
        row = row + 1; // skip the table header row.
        String[] content = new String[4];
        content[0] = browser.getCellContents("config-table", row, 0);
        content[1] = browser.getCellContents("config-table", row, 1);
        content[2] = browser.getCellContents("config-table", row, 2);
        content[3] = browser.getCellContents("config-table", row, 3);
        return content;
    }

    public DependencyForm clickView(String baseName)
    {
        String actionId = ListPage.ACTION_VIEW + ":" + baseName;
        if (!browser.isElementPresent(actionId))
        {
            throw new RuntimeException();
        }
        browser.click(actionId);
        return browser.createForm(DependencyForm.class);
    }

}
