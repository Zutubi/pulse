package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.Pager;
import com.zutubi.pulse.acceptance.components.pulse.project.BuildSummaryTable;
import com.zutubi.pulse.acceptance.components.table.PropertyTable;
import com.zutubi.pulse.acceptance.components.table.SummaryTable;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.EnumUtils;
import com.zutubi.util.WebUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Page for viewing details of a single changelist.
 */
public class ViewChangelistPage extends SeleniumPage
{
    private String projectName;
    private long buildId;
    private long changeId;
    private PropertyTable detailsTable;
    private BuildSummaryTable buildsTable;
    private SummaryTable filesTable;
    private Pager pager;

    public ViewChangelistPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId, long changeId, String revisionString)
    {
        super(browser, urls, "changelist-" + revisionString, "changelist " + revisionString);
        this.projectName = projectName;
        this.buildId = buildId;
        this.changeId = changeId;

        detailsTable = new PropertyTable(browser, "changelist-changelist");
        buildsTable = new BuildSummaryTable(browser, "changelist-builds");
        filesTable = new SummaryTable(browser, "changelist-files");
        pager = new Pager(browser, "changelist-pager");
    }

    public String getUrl()
    {
        return urls.buildChangelist(WebUtils.uriComponentEncode(projectName), Long.toString(buildId), changeId);
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("panel");
        browser.waitForVariable("panel.initialised");
    }

    public String getRevision()
    {
        return detailsTable.getValue("revision");
    }

    public String getAuthor()
    {
        return detailsTable.getValue("who");
    }

    public String getComment()
    {
        return detailsTable.getValue("comment");
    }

    /**
     * Returns the number of builds affected by this change.
     * 
     * @return the number of builds that are linked to this change
     */
    public int getBuildsCount()
    {
        return buildsTable.getRowCount();
    }

    /**
     * Returns details of all builds affected by this change, in the order that
     * they are found on the page.
     * 
     * @return info about the affected builds
     */
    public List<BuildInfo> getBuilds()
    {
        return buildsTable.getBuilds();
    }

    public int getFilesCount()
    {
        return filesTable.getRowCount();
    }

    public Changelist getChangelist()
    {
        List<FileChange> fileChanges = new LinkedList<FileChange>();
        int filesCount = getFilesCount();
        for (int i = 0; i < filesCount; i++)
        {
            Map<String,String> row = filesTable.getRow(i);
            fileChanges.add(new FileChange(
                    row.get("fileName"),
                    new Revision(row.get("revision")),
                    EnumUtils.fromPrettyString(FileChange.Action.class, row.get("action"))
            ));
        }

        return new Changelist(new Revision(getRevision()), 0, getAuthor(), getComment(), fileChanges);
    }

    public boolean isNextLinkPresent()
    {
        return pager.hasNextLink();
    }

    public void clickNext()
    {
        pager.clickNext();
    }

    public boolean isPreviousLinkPresent()
    {
        return pager.hasPreviousLink();
    }
}
