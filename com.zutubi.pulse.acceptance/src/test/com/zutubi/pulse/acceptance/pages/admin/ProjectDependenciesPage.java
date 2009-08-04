package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.IDs;
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
     * @return the contents of the row
     */
    public DependencyRow getDependencyRow(int row)
    {
        row = row + 1; // skip the table header row.
        return new DependencyRow(
                browser.getCellContents(IDs.COLLECTION_TABLE, row, 0),
                browser.getCellContents(IDs.COLLECTION_TABLE, row, 1),
                browser.getCellContents(IDs.COLLECTION_TABLE, row, 2),
                browser.getCellContents(IDs.COLLECTION_TABLE, row, 3)
        );
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

    public class DependencyRow
    {
        String projectName;
        String revision;
        String stageList;
        String transitive;

        public DependencyRow(String projectName, String revision, String stageList, String transitive)
        {
            this.projectName = projectName;
            this.revision = revision;
            this.stageList = stageList;
            this.transitive = transitive;
        }

        public String getProjectName()
        {
            return projectName;
        }

        public String getRevision()
        {
            return revision;
        }

        public String getStageList()
        {
            return stageList;
        }

        public String getTransitive()
        {
            return transitive;
        }
    }
}
