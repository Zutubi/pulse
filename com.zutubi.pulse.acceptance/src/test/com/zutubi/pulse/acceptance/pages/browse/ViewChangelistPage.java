package com.zutubi.pulse.acceptance.pages.browse;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.acceptance.SeleniumUtils;
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

    public ViewChangelistPage(Selenium selenium, Urls urls, String projectName, long buildId, long changeId, String revisionString)
    {
        super(selenium, urls, "changelist." + revisionString, "changelist " + revisionString);
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
        return SeleniumUtils.getCellContents(selenium, ID_DETAILS_TABLE, 1, 1);
    }

    public String getAuthor()
    {
        return SeleniumUtils.getCellContents(selenium, ID_DETAILS_TABLE, 2, 1);
    }

    public String getComment()
    {
        return SeleniumUtils.getCellContents(selenium, ID_DETAILS_TABLE, 4, 1);
    }

    public int getFilesCount()
    {
        int count = 1;
        while (selenium.isElementPresent(getChangelistFileId(count)))
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
                    SeleniumUtils.getCellContents(selenium, ID_FILES_TABLE, i + 2, 0),
                    SeleniumUtils.getCellContents(selenium, ID_FILES_TABLE, i + 2, 1),
                    FileChange.Action.fromString(SeleniumUtils.getCellContents(selenium, ID_FILES_TABLE, i + 2, 2))
            ));
        }

        return new Changelist(new Revision(getRevision()), 0, getAuthor(), getComment(), fileChanges);
    }

}
