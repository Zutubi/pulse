package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The changes tab for a build result.
 */
public class BuildChangesPage extends SeleniumPage
{
    private static final String ID_CHANGELIST_TABLE = "changelist.summaries";

    private static final String FORMAT_ID_CHANGELIST_ROW   = "change.%d";
    private static final String FORMAT_ID_CHANGELIST_FILES = "change.%d.file.%d";

    private static final Pattern FILE_CHANGE_PATTERN = Pattern.compile("(.+) #(.+) - (.+)");

    private static final String PREFIX_VIEW_LINK = "view.";

    private String projectName;
    private long buildId;

    public BuildChangesPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-changes", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildChanges(projectName, Long.toString(buildId));
    }

    public static String formatChangesHeader(long buildNumber)
    {
        return "changes between build " + Long.toString(buildNumber - 1) + " and build " + buildNumber;
    }

    public boolean hasChanges()
    {
        return browser.isElementIdPresent(ID_CHANGELIST_TABLE);
    }

    public String getChangesHeader()
    {
        return browser.getCellContents(ID_CHANGELIST_TABLE, 0, 0);
    }

    private String getChangelistRowId(int changeNumber)
    {
        return String.format(FORMAT_ID_CHANGELIST_ROW , changeNumber);
    }

    private String getChangelistFileId(int changeNumber, int fileNumber)
    {
        return String.format(FORMAT_ID_CHANGELIST_FILES, changeNumber, fileNumber);
    }

    public int getChangeCount()
    {
        int count = 1;
        while (browser.isElementIdPresent(getChangelistRowId(count)))
        {
            count++;
        }

        return count - 1;
    }

    public int getFilesCount(int changeNumber)
    {
        int count = 1;
        while (browser.isElementIdPresent(getChangelistFileId(changeNumber, count)))
        {
            count++;
        }

        return count - 1;
    }

    public List<Long> getChangeIds()
    {
        List<String> viewIdStrings = CollectionUtils.filter(browser.getAllLinks(), new Predicate<String>()
        {
            public boolean satisfied(String s)
            {
                return s.startsWith(PREFIX_VIEW_LINK);
            }
        });
        
        return CollectionUtils.map(viewIdStrings, new Mapping<String, Long>()
        {
            public Long map(String s)
            {
                return Long.parseLong(s.substring(PREFIX_VIEW_LINK.length()));
            }
        });
    }

    public List<Changelist> getChangelists()
    {
        List<Changelist> changelists = new LinkedList<Changelist>();
        int changeCount = getChangeCount();
        for (int i = 0; i < changeCount; i++)
        {
            List<FileChange> fileChanges = new LinkedList<FileChange>();
            int filesCount = getFilesCount(i + 1);
            for (int j = 0; j < filesCount; j++)
            {
                String fileString = browser.getText(getChangelistFileId(i + 1, j + 1));
                Matcher matcher = FILE_CHANGE_PATTERN.matcher(fileString);
                if (!matcher.matches())
                {
                    throw new RuntimeException("File string '" + fileString + "' does not match expected format");
                }

                fileChanges.add(new FileChange(matcher.group(1), new Revision(matcher.group(2)), FileChange.Action.fromString(matcher.group(3))));
            }


            int row = i * 3 + 2;
            Changelist changelist = new Changelist(
                    new Revision(browser.getCellContents(ID_CHANGELIST_TABLE, row, 0)),
                    0,
                    browser.getCellContents(ID_CHANGELIST_TABLE, row, 1),
                    browser.getCellContents(ID_CHANGELIST_TABLE, row, 3),
                    fileChanges
            );

            changelists.add(changelist);
        }

        return changelists;
    }

}
