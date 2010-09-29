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
import com.zutubi.util.WebUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The changes tab for a build result.
 */
public class BuildChangesPage extends SeleniumPage
{
    private static final String ID_CHANGELIST_TABLES = "changelist-tables";
    private static final String ID_COMPARE_TO_POPUP = "compare-to";
    private static final String ID_COMPARE_TO_POPUP_BUTTON = "compare-to-button";

    private static final String FORMAT_ID_CHANGELIST = "change.%d";
    private static final String FORMAT_ID_CHANGELIST_COMMENT = "change.%d.comment";
    private static final String FORMAT_ID_CHANGELIST_FILES = "change.%d.file.%d";

    private static final Pattern FILE_CHANGE_PATTERN = Pattern.compile("(.+) #(.+) - (.+)");

    private static final String PREFIX_VIEW_LINK = "view.";

    private String projectName;
    private long buildId;

    public BuildChangesPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, projectName + "-build-" + Long.toString(buildId) + "-changes", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildChanges(WebUtils.uriComponentEncode(projectName), Long.toString(buildId));
    }

    public static String formatChangesSince(long buildNumber)
    {
        return "Showing changes since: build " + (buildNumber - 1);
    }

    public boolean hasChanges()
    {
        return browser.isElementIdPresent(ID_CHANGELIST_TABLES);
    }

    private String getChangelistId(int changeNumber)
    {
        return String.format(FORMAT_ID_CHANGELIST, changeNumber);
    }

    private String getChangelistCommentId(int changeNumber)
    {
        return String.format(FORMAT_ID_CHANGELIST_COMMENT, changeNumber);
    }

    private String getChangelistFileId(int changeNumber, int fileNumber)
    {
        return String.format(FORMAT_ID_CHANGELIST_FILES, changeNumber, fileNumber);
    }

    public int getChangeCount()
    {
        int count = 1;
        while (browser.isElementIdPresent(getChangelistId(count)))
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

            String changeId = getChangelistId(i + 1);
            String changeHeader = browser.getCellContents(changeId, 0, 0);
            String[] headerPieces = changeHeader.split("\\s+");
            Changelist changelist = new Changelist(
                    new Revision(headerPieces[0]),
                    0,
                    headerPieces[1],
                    browser.getText(getChangelistCommentId(i + 1)),
                    fileChanges
            );

            changelists.add(changelist);
        }

        return changelists;
    }

    public boolean isCompareToPopDownPresent()
    {
        return browser.isElementPresent(ID_COMPARE_TO_POPUP_BUTTON);
    }
    
    /**
     * Clicks the button link to pop down the compare to box.  I would like to
     * add waiting for it to appear, but this fails in Selenium (it keeps
     * waiting, although I can see the popdown).
     */
    public void clickCompareToPopDown()
    {
        browser.click(ID_COMPARE_TO_POPUP_BUTTON);
    }
}
