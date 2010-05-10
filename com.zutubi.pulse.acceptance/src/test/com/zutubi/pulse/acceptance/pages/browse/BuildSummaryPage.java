package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.ConfirmDialog;
import com.zutubi.pulse.master.webwork.Urls;
import static com.zutubi.util.WebUtils.uriComponentEncode;

/**
 * The summary tab for a build result.
 */
public class BuildSummaryPage extends AbstractBuildStatusPage
{
    private static final String ID_COMMENTS_LIST = "build-comments";
    private static final String ID_TEST_FAILURES = "failed-tests";
    private static final String ID_RELATED_LINKS = "related-links";
    private static final String ID_FEATURED_ARTIFACTS = "featured-artifacts";

    public BuildSummaryPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, projectName + "-build-" + Long.toString(buildId) + "-summary", "build " + buildId, projectName, buildId);
    }

    public String getUrl()
    {
        return urls.buildSummary(uriComponentEncode(projectName), Long.toString(buildId));
    }

    private String getHookId(String hookName)
    {
        return "hook." + hookName;
    }

    public boolean isHookPresent(String hookName)
    {
        return browser.isElementIdPresent(getHookId(hookName));
    }

    public void clickHook(String hookName)
    {
        browser.click(getHookId(hookName));
    }

    public String getSummaryTestsColumnText()
    {
        return browser.getCellContents(ID_BUILD_BASICS, 4, 1);
    }

    public boolean isCommentsPresent()
    {
        return browser.isElementPresent(ID_COMMENTS_LIST);
    }

    public boolean isDeleteCommentLinkPresent(int commentNumber)
    {
        return browser.isElementPresent(getDeleteCommentLinkId(commentNumber));
    }

    public ConfirmDialog clickDeleteComment(int commentNumber)
    {
        browser.click(getDeleteCommentLinkId(commentNumber));
        return new ConfirmDialog(browser);
    }

    public boolean isTestFailuresTablePresent()
    {
        return browser.isElementPresent(ID_TEST_FAILURES);
    }

    private String getDeleteCommentLinkId(int commentNumber)
    {
        return "delete.comment." + commentNumber;
    }

    public boolean isRelatedLinksTablePresent()
    {
        return browser.isElementPresent(ID_RELATED_LINKS);
    }
    
    public String getRelatedLinkText(int index)
    {
        return browser.getCellContents(ID_RELATED_LINKS, index + 1, 0);
    }

    public boolean isFeaturedArtifactsTablePresent()
    {
        return browser.isElementPresent(ID_FEATURED_ARTIFACTS);
    }

    public String getFeaturedArtifactsRow(int index)
    {
        return browser.getCellContents(ID_FEATURED_ARTIFACTS, index + 1, 0);
    }
}
