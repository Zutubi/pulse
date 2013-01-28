package com.zutubi.pulse.acceptance.pages.browse;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.WebUtils;
import com.zutubi.util.adt.Pair;
import static java.util.Arrays.asList;
import org.openqa.selenium.By;

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

    private static final String FORMAT_ID_CHANGELIST = "%s.%d";
    private static final String FORMAT_ID_CHANGELIST_COMMENT = "%s.%d.comment";
    private static final String FORMAT_ID_CHANGELIST_FILES = "%s.%d.file.%d";
    private static final String FORMAT_ID_CHANGELIST_VIA = "%s.%d.via.%d";

    private static final Pattern PATTERN_FILE_CHANGE = Pattern.compile("(.+) #(.+) - (.+)");
    private static final Pattern PATTERN_VIA = Pattern.compile("(.+) :: build (\\d+)");

    private static final String PREFIX_REGULAR_CHANGE = "change";
    private static final String PREFIX_UPSTREAM_CHANGE = "upstream";
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

    public String formatChangesSince(long buildNumber)
    {
        return "Showing changes since: build " + (buildNumber - 1);
    }

    public boolean hasChanges()
    {
        return browser.isElementIdPresent(ID_CHANGELIST_TABLES);
    }

    private String getChangelistId(String prefix, int changeNumber)
    {
        return String.format(FORMAT_ID_CHANGELIST, prefix, changeNumber);
    }

    private String getChangelistCommentId(String prefix, int changeNumber)
    {
        return String.format(FORMAT_ID_CHANGELIST_COMMENT, prefix, changeNumber);
    }

    private String getChangelistFileId(String prefix, int changeNumber, int fileNumber)
    {
        return String.format(FORMAT_ID_CHANGELIST_FILES, prefix, changeNumber, fileNumber);
    }

    private String getChangelistViaId(int changeNumber, int viaNumber)
    {
        return String.format(FORMAT_ID_CHANGELIST_VIA, PREFIX_UPSTREAM_CHANGE, changeNumber, viaNumber);
    }

    /**
     * @return the number of direct changes displayed on the page
     */
    public int getChangeCount()
    {
        return getChangeCount(PREFIX_REGULAR_CHANGE);
    }

    /**
     * @return the number of changes via upstream dependencies displayed on the page
     */
    public int getUpstreamChangeCount()
    {
        return getChangeCount(PREFIX_UPSTREAM_CHANGE);
    }

    private int getChangeCount(String prefix)
    {
        int count = 1;
        while (browser.isElementIdPresent(getChangelistId(prefix, count)))
        {
            count++;
        }

        return count - 1;
    }

    /**
     * Indicates how many files are shown for a given changelist.
     * 
     * @param changeNumber 1-based number of the change on the page
     * @return number of files displayed for the change
     */
    public int getFilesCount(int changeNumber)
    {
        return getFilesCount(PREFIX_REGULAR_CHANGE, changeNumber);
    }

    /**
     * Indicates how many files are shown for a given upstream changelist.
     *
     * @param changeNumber 1-based number of the upstream change on the page
     * @return number of files displayed for the change
     */
    public int getUpstreamFilesCount(int changeNumber)
    {
        return getFilesCount(PREFIX_UPSTREAM_CHANGE, changeNumber);
    }

    private int getFilesCount(String prefix, int changeNumber)
    {
        int count = 1;
        while (browser.isElementIdPresent(getChangelistFileId(prefix, changeNumber, count)))
        {
            count++;
        }

        return count - 1;
    }

    public List<Long> getChangeIds()
    {
        Iterable<String> viewIdStrings = Iterables.filter(asList(browser.getAllLinks()), new Predicate<String>()
        {
            public boolean apply(String s)
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

    /**
     * @return all direct changes listed on the page
     */
    public List<Changelist> getChangelists()
    {
        return getChangelists(PREFIX_REGULAR_CHANGE);
    }

    /**
     * @return all changes via upstream dependencies listed on the page
     */
    public List<Changelist> getUpstreamChangelists()
    {
        return getChangelists(PREFIX_UPSTREAM_CHANGE);
    }

    private List<Changelist> getChangelists(String prefix)
    {
        List<Changelist> changelists = new LinkedList<Changelist>();
        int changeCount = getChangeCount();
        for (int i = 0; i < changeCount; i++)
        {
            List<FileChange> fileChanges = new LinkedList<FileChange>();
            int filesCount = getFilesCount(i + 1);
            for (int j = 0; j < filesCount; j++)
            {
                String fileString = browser.getText(By.id(getChangelistFileId(prefix, i + 1, j + 1)));
                Matcher matcher = PATTERN_FILE_CHANGE.matcher(fileString);
                if (!matcher.matches())
                {
                    throw new RuntimeException("File string '" + fileString + "' does not match expected format");
                }

                fileChanges.add(new FileChange(matcher.group(1), new Revision(matcher.group(2)), FileChange.Action.fromString(matcher.group(3))));
            }

            String changeId = getChangelistId(prefix, i + 1);
            String changeHeader = browser.getCellContents(changeId, 0, 0);
            String[] headerPieces = changeHeader.split("\\s+");
            Changelist changelist = new Changelist(
                    new Revision(headerPieces[0]),
                    0,
                    headerPieces[1],
                    browser.getText(By.id(getChangelistCommentId(prefix, i + 1))),
                    fileChanges
            );

            changelists.add(changelist);
        }

        return changelists;
    }

    /**
     * Indicates how many build paths an upstream change was found via.
     * 
     * @param changeNumber 1-based number of the upstream change on the page
     * @return number of build paths the change was found via
     */
    public int getViaCount(int changeNumber)
    {
        int count = 1;
        while (browser.isElementIdPresent(getChangelistViaId(changeNumber, count)))
        {
            count++;
        }

        return count - 1;
    }

    /**
     * Gets the build paths via which an upstream change was found.  Each element of the returned
     * list is a build path, where each build path is a list of (project name, build number) pairs.
     * 
     * @param changeNumber 1-based number of the upstream change on the page
     * @return all build paths via which the change was found
     */
    public List<List<Pair<String, Long>>> getUpstreamChangeVia(int changeNumber)
    {
        List<List<Pair<String, Long>>> vias = new LinkedList<List<Pair<String, Long>>>();
        for (int i = 1; i <= getViaCount(changeNumber); i++)
        {
            List<Pair<String, Long>> via = new LinkedList<Pair<String, Long>>();
            String viaString = browser.getText(By.id(getChangelistViaId(changeNumber, i)));
            String[] pieces = viaString.split(">");
            for (String piece: pieces)
            {
                Matcher matcher = PATTERN_VIA.matcher(piece.trim());
                if (!matcher.matches())
                {
                    throw new RuntimeException("Via string '" + viaString + "' does not match expected format");
                }

                via.add(CollectionUtils.asPair(matcher.group(1), Long.parseLong(matcher.group(2))));
            }
            
            vias.add(via);
        }
        
        return vias;
    }

    public boolean isCompareToPopDownPresent()
    {
        return browser.isElementIdPresent(ID_COMPARE_TO_POPUP_BUTTON);
    }
    
    /**
     * Clicks the button link to pop down the compare to box.  I would like to
     * add waiting for it to appear, but this fails in Selenium (it keeps
     * waiting, although I can see the popdown).
     */
    public void clickCompareToPopDown()
    {
        browser.click(By.id(ID_COMPARE_TO_POPUP_BUTTON));
    }
}
