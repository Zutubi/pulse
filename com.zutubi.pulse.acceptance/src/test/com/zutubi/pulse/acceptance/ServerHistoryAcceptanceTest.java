package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BuildInfo;
import com.zutubi.pulse.acceptance.pages.server.ServerHistoryPage;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.xwork.actions.ajax.HistoryDataAction;
import com.zutubi.tove.type.record.PathUtils;

import java.util.LinkedList;
import java.util.List;

import static com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration.DEFAULT_HISTORY_BUILDS_PER_PAGE;
import static java.util.Arrays.asList;

/**
 * Acceptance tests for the server section of the reporting UI.
 */
public class ServerHistoryAcceptanceTest extends HistoryAcceptanceTestBase
{
    public void setUp() throws Exception
    {
        super.setUp();

        rpcClient.RemoteApi.deleteAllConfigs(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    public void testNoBuilds() throws Exception
    {
        getBrowser().loginAsAdmin();

        ServerHistoryPage historyPage = getBrowser().openAndWaitFor(ServerHistoryPage.class);
        assertEquals(HistoryDataAction.STATE_ANY, historyPage.getStateFilter());
        assertEmptyHistory(historyPage);

        setFilterAndWait(historyPage, HistoryDataAction.STATE_BROKEN);
        assertEquals(HistoryDataAction.STATE_BROKEN, historyPage.getStateFilter());
        assertEmptyHistory(historyPage);

        historyPage.clearStateFilter();
        historyPage.waitFor();
        assertEquals(HistoryDataAction.STATE_ANY, historyPage.getStateFilter());
        assertEmptyHistory(historyPage);
    }

    public void testMultipleProjects() throws Exception
    {
        String project1 = random + "-1";
        String project2 = random + "-2";

        rpcClient.RemoteApi.insertSimpleProject(project1);
        rpcClient.RemoteApi.insertSimpleProject(project2);
        rpcClient.RemoteApi.runBuild(project1, BUILD_TIMEOUT);
        rpcClient.RemoteApi.runBuild(project1, BUILD_TIMEOUT);
        rpcClient.RemoteApi.runBuild(project2, BUILD_TIMEOUT);

        getBrowser().loginAsAdmin();

        ServerHistoryPage historyPage = getBrowser().openAndWaitFor(ServerHistoryPage.class);
        List<BuildInfo> expectedBuilds = asList(
                new BuildInfo(project2, 1, ResultState.SUCCESS, rpcClient.RemoteApi.getBuildRevision(project2, 1)),
                new BuildInfo(project1, 2, ResultState.SUCCESS, rpcClient.RemoteApi.getBuildRevision(project1, 2)),
                new BuildInfo(project1, 1, ResultState.SUCCESS, rpcClient.RemoteApi.getBuildRevision(project1, 1))
        );
        assertBuildHistory(historyPage, expectedBuilds);

        setFilterAndWait(historyPage, HistoryDataAction.STATE_BROKEN);
        assertEmptyHistory(historyPage);

        setFilterAndWait(historyPage, HistoryDataAction.STATE_SUCCESS);
        assertBuildHistory(historyPage, expectedBuilds);
    }

    public void testMultiplePages() throws Exception
    {
        final int BUILD_COUNT = 13;

        rpcClient.RemoteApi.insertSimpleProject(random);

        List<BuildInfo> builds = new LinkedList<BuildInfo>();
        for (int i = 0; i < BUILD_COUNT; i++)
        {
            int buildNumber = rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);
            builds.add(0, new BuildInfo(random, buildNumber, ResultState.SUCCESS, rpcClient.RemoteApi.getBuildRevision(random, buildNumber)));
        }

        getBrowser().loginAsAdmin();

        ServerHistoryPage historyPage = getBrowser().openAndWaitFor(ServerHistoryPage.class);
        assertEquals(DEFAULT_HISTORY_BUILDS_PER_PAGE, historyPage.getBuildCount());
        assertFirstPage(historyPage, BUILD_COUNT);
        assertEquals(builds.subList(0, DEFAULT_HISTORY_BUILDS_PER_PAGE), historyPage.getBuilds());

        // Step forward
        historyPage.getPager().clickNext();
        historyPage.waitFor();

        assertEquals(BUILD_COUNT - DEFAULT_HISTORY_BUILDS_PER_PAGE, historyPage.getBuildCount());
        assertLastPage(historyPage, 2, BUILD_COUNT);
        assertEquals(builds.subList(DEFAULT_HISTORY_BUILDS_PER_PAGE, builds.size()), historyPage.getBuilds());

        // Step backward
        historyPage.getPager().clickFirst();
        historyPage.waitFor();

        assertEquals(DEFAULT_HISTORY_BUILDS_PER_PAGE, historyPage.getBuildCount());
        assertFirstPage(historyPage, BUILD_COUNT);
        assertEquals(builds.subList(0, DEFAULT_HISTORY_BUILDS_PER_PAGE), historyPage.getBuilds());
    }
}
