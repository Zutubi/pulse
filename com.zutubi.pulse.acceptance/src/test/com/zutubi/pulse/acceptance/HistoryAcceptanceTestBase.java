package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.components.Pager;
import com.zutubi.pulse.acceptance.pages.AbstractHistoryPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildInfo;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import static java.util.Arrays.asList;

import java.util.List;

/**
 * Helper base class for build history acceptance tests.
 */
public class HistoryAcceptanceTestBase extends AcceptanceTestBase
{
    protected static final long BUILD_TIMEOUT = 90000;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    protected void setFilterAndWait(AbstractHistoryPage historyPage, String stateFilter)
    {
        historyPage.setStateFilter(stateFilter);
        getBrowser().waitForPageToLoad();
        historyPage.waitFor();
    }

    protected void assertEmptyHistory(AbstractHistoryPage historyPage)
    {
        assertEquals(0, historyPage.getBuildCount());
        assertNoPaging(historyPage, 0);
    }

    protected void assertBuildHistory(AbstractHistoryPage historyPage, List<BuildInfo> builds)
    {
        assertEquals(builds.size(), historyPage.getBuildCount());
        if (builds.size() <= UserPreferencesConfiguration.DEFAULT_HISTORY_BUILDS_PER_PAGE)
        {
            assertNoPaging(historyPage, builds.size());
        }

        assertEquals(builds, historyPage.getBuilds());
    }

    protected void assertSingleBuildHistory(AbstractHistoryPage historyPage, BuildInfo build)
    {
        assertBuildHistory(historyPage, asList(build));
    }

    protected void assertNoPaging(AbstractHistoryPage historyPage, int totalItems)
    {
        Pager pager = historyPage.getPager();
        assertEquals(0, pager.getCurrentPage());
        assertEquals(totalItems, pager.getTotalItems());
        assertFalse(pager.hasPagingRow());
    }

    protected void assertFirstPage(AbstractHistoryPage historyPage, int totalItems)
    {
        Pager pager = historyPage.getPager();
        assertEquals(0, pager.getCurrentPage());
        assertEquals(totalItems, pager.getTotalItems());
        assertTrue(pager.hasPagingRow());
        assertFalse(pager.hasFirstLink());
        assertFalse(pager.hasPreviousLink());
        assertTrue(pager.hasNextLink());
        assertTrue(pager.hasLastLink());
    }

    protected void assertLastPage(AbstractHistoryPage historyPage, int totalPages, int totalItems)
    {
        Pager pager = historyPage.getPager();
        assertEquals(totalPages - 1, pager.getCurrentPage());
        assertEquals(totalItems, pager.getTotalItems());
        assertTrue(pager.hasPagingRow());
        assertTrue(pager.hasFirstLink());
        assertTrue(pager.hasPreviousLink());
        assertFalse(pager.hasNextLink());
        assertFalse(pager.hasLastLink());
    }
}
