package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.webwork.Urls;

import java.util.LinkedList;
import java.util.List;

/**
 * Page for viewing details of a single changelist.
 */
public class ViewChangelistPage extends SeleniumPage
{
    private static final String ID_DETAILS_TABLE = "changelist.details";
    private static final String ID_FILES_TABLE   = "changelist.files";

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

    public String getChangelistFileId(int fileNumber)
    {
        return String.format(FORMAT_ID_CHANGELIST_FILES, fileNumber);
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

    public int getFilesCount()
    {
        int count = 1;
        while (browser.isElementIdPresent(getChangelistFileId(count)))
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

}
