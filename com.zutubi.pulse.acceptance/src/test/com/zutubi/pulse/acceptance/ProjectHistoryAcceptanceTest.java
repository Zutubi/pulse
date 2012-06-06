package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BuildInfo;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHistoryPage;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.xwork.actions.ajax.HistoryDataAction;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import static com.zutubi.pulse.acceptance.Constants.Project.AntCommand.TARGETS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.RECIPES;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.COMMANDS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND;
import static com.zutubi.pulse.acceptance.Constants.Project.TYPE;
import static com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration.DEFAULT_HISTORY_BUILDS_PER_PAGE;

public class ProjectHistoryAcceptanceTest extends HistoryAcceptanceTestBase
{
    public void testNewProject() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random);
        getBrowser().loginAsAdmin();

        ProjectHistoryPage historyPage = getBrowser().openAndWaitFor(ProjectHistoryPage.class, random);
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

    public void testSingleBuild() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random);
        int buildNumber = rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);
        getBrowser().loginAsAdmin();

        ProjectHistoryPage historyPage = getBrowser().openAndWaitFor(ProjectHistoryPage.class, random);
        BuildInfo build = new BuildInfo(buildNumber, ResultState.SUCCESS, rpcClient.RemoteApi.getBuildRevision(random, buildNumber));
        assertSingleBuildHistory(historyPage, build);

        setFilterAndWait(historyPage, HistoryDataAction.STATE_BROKEN);
        assertEmptyHistory(historyPage);

        setFilterAndWait(historyPage, HistoryDataAction.STATE_SUCCESS);
        assertSingleBuildHistory(historyPage, build);
    }
    
    public void testMultiplePages() throws Exception
    {
        final int BUILD_COUNT = 13;
        final int INDEX_FAILED_BUILD = 4;
        final int INDEX_ERROR_BUILD = 8;
        
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random);
        String antCommandPath = PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND);
        
        Hashtable<String, Object> originalAntConfig = rpcClient.RemoteApi.getConfig(antCommandPath);
        originalAntConfig.put(TARGETS, "");
        originalAntConfig.put("exe", "");
        
        List<BuildInfo> builds = new LinkedList<BuildInfo>();
        for (int i = 0; i < BUILD_COUNT; i++)
        {
            ResultState expectedState = ResultState.SUCCESS;
            if (i == INDEX_FAILED_BUILD)
            {
                Hashtable<String, Object> antConfig = rpcClient.RemoteApi.getConfig(antCommandPath);
                antConfig.put(TARGETS, "unknown");
                rpcClient.RemoteApi.saveConfig(antCommandPath, antConfig, false);
                expectedState = ResultState.FAILURE;
            }
            else if (i == INDEX_ERROR_BUILD)
            {
                Hashtable<String, Object> antConfig = rpcClient.RemoteApi.getConfig(antCommandPath);
                antConfig.put("exe", "nosuchantcomment");
                rpcClient.RemoteApi.saveConfig(antCommandPath, antConfig, false);
                expectedState = ResultState.ERROR;
            }

            int buildNumber = rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);
            builds.add(0, new BuildInfo(buildNumber, expectedState, rpcClient.RemoteApi.getBuildRevision(random, buildNumber)));
            
            if (expectedState != ResultState.SUCCESS)
            {
                rpcClient.RemoteApi.saveConfig(antCommandPath, originalAntConfig, false);
            }
        }

        
        getBrowser().loginAsAdmin();

        ProjectHistoryPage historyPage = getBrowser().openAndWaitFor(ProjectHistoryPage.class, random);
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
        
        // Filter to broken
        setFilterAndWait(historyPage, HistoryDataAction.STATE_BROKEN);
        List<BuildInfo> brokenBuilds = CollectionUtils.filter(builds, new Predicate<BuildInfo>()
        {
            public boolean satisfied(BuildInfo buildInfo)
            {
                return buildInfo.status != ResultState.SUCCESS;
            }
        });
        
        assertEquals(brokenBuilds.size(), historyPage.getBuildCount());
        assertNoPaging(historyPage, 2);
        assertEquals(brokenBuilds, historyPage.getBuilds());
        
        // Filter to successful (still multiple pages)
        setFilterAndWait(historyPage, HistoryDataAction.STATE_SUCCESS);
        List<BuildInfo> successfulBuilds = CollectionUtils.filter(builds, new Predicate<BuildInfo>()
        {
            public boolean satisfied(BuildInfo buildInfo)
            {
                return buildInfo.status == ResultState.SUCCESS;
            }
        });
        
        assertEquals(DEFAULT_HISTORY_BUILDS_PER_PAGE, historyPage.getBuildCount());
        assertFirstPage(historyPage, successfulBuilds.size());
        assertEquals(successfulBuilds.subList(0, DEFAULT_HISTORY_BUILDS_PER_PAGE), historyPage.getBuilds());
        
        // Step forward, ensure the filter is maintained.
        historyPage.getPager().clickLast();
        historyPage.waitFor();

        assertEquals(successfulBuilds.size() - DEFAULT_HISTORY_BUILDS_PER_PAGE, historyPage.getBuildCount());
        assertLastPage(historyPage, 2, successfulBuilds.size());
        assertEquals(successfulBuilds.subList(DEFAULT_HISTORY_BUILDS_PER_PAGE, successfulBuilds.size()), historyPage.getBuilds());
    }
}
