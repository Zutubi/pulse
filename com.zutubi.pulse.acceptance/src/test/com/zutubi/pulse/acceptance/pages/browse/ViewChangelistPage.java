package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.Pair;

import java.util.LinkedList;
import java.util.List;

/**
 * Page for viewing details of a single changelist.
 */
public class ViewChangelistPage extends SeleniumPage
{
    private static final String ID_DETAILS_TABLE = "changelist.details";
    private static final String ID_BUILDS_TABLE  = "changelist.builds";
    private static final String ID_FILES_TABLE   = "changelist.files";
    private static final String ID_NEXT_PAGE     = "page.next";
    private static final String ID_PREVIOUS_PAGE = "page.previous";

    private static final String FORMAT_ID_CHANGELIST_BUILDS = "changelist.build.%d";
    private static final String FORMAT_ID_CHANGELIST_FILES = "changelist.file.%d";

    private String projectName;
    private long buildId;
    private long changeId;

    public ViewChangelistPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId, long changeId, String revisionString)
    {
        super(browser, urls, "changelist." + revisionString, "changelist " + revisionString);
        this.projectName = projectName;
        this.buildId = buildId;
        this.changeId = changeId;
    }

    public String getUrl()
    {
        return urls.buildChangelist(projectName, Long.toString(buildId), changeId);
    }

    public String getRevision()
    {
        return browser.getCellContents(ID_DETAILS_TABLE, 1, 1);
    }

    public String getAuthor()
    {
        return browser.getCellContents(ID_DETAILS_TABLE, 2, 1);
    }

    public String getComment()
    {
        return browser.getCellContents(ID_DETAILS_TABLE, 4, 1);
    }

    /**
     * Returns the number of builds affected by this change.
     * 
     * @return the number of builds that are linked to this change
     */
    public int getBuildsCount()
    {
        return getCount(FORMAT_ID_CHANGELIST_BUILDS);
    }

    /**
     * Returns details of all builds affected by this change, in the order that
     * they are found on the page.
     * 
     * @return the affected builds, each pair is a project name and build
     *         number
     */
    public List<Pair<String, Long>> getBuilds()
    {
        List<Pair<String, Long>> builds = new LinkedList<Pair<String, Long>>();
        int count = getBuildsCount();
        for (int i = 0; i < count; i++)
        {
            String project = browser.getCellContents(ID_BUILDS_TABLE, i + 2, 0);
            String numberString = browser.getCellContents(ID_BUILDS_TABLE, i + 2, 1);
            builds.add(new Pair<String, Long>(project, Long.parseLong(numberString)));
        }
        
        return builds;
    }

    public int getFilesCount()
    {
        return getCount(FORMAT_ID_CHANGELIST_FILES);
    }

    private int getCount(String idFormatString)
    {
        int count = 1;
        while (browser.isElementIdPresent(String.format(idFormatString, count)))
        {
            count++;
        }

        return count - 1;
    }

    public Changelist getChangelist()
    {
        List<FileChange> fileChanges = new LinkedList<FileChange>();
        int filesCount = getFilesCount();
        for (int i = 0; i < filesCount; i++)
        {
            fileChanges.add(new FileChange(
                    browser.getCellContents(ID_FILES_TABLE, i + 2, 0),
                    new Revision(browser.getCellContents(ID_FILES_TABLE, i + 2, 1)),
                    FileChange.Action.fromString(browser.getCellContents(ID_FILES_TABLE, i + 2, 2))
            ));
        }

        return new Changelist(new Revision(getRevision()), 0, getAuthor(), getComment(), fileChanges);
    }

    public boolean isNextLinkPresent()
    {
        return browser.isElementPresent(ID_NEXT_PAGE);
    }

    public void clickNext()
    {
        browser.click(ID_NEXT_PAGE);
    }

    public boolean isPreviousLinkPresent()
    {
        return browser.isElementPresent(ID_PREVIOUS_PAGE);
    }
}
