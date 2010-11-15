package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.components.Pager;
import com.zutubi.pulse.acceptance.pages.browse.BuildInfo;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHistoryPage;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.xwork.actions.project.ProjectHistoryDataAction;
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
import static java.util.Arrays.asList;

public class ProjectHistoryAcceptanceTest extends AcceptanceTestBase
{
    private static final long BUILD_TIMEOUT = 90000;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testNewProject() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random);
        getBrowser().loginAsAdmin();

        ProjectHistoryPage historyPage = getBrowser().openAndWaitFor(ProjectHistoryPage.class, random);
        assertEquals(ProjectHistoryDataAction.STATE_ANY, historyPage.getStateFilter());
        assertEmptyHistory(historyPage);

        setFilterAndWait(historyPage, ProjectHistoryDataAction.STATE_BROKEN);
        assertEquals(ProjectHistoryDataAction.STATE_BROKEN, historyPage.getStateFilter());
        assertEmptyHistory(historyPage);
        
        historyPage.clearStateFilter();
        getBrowser().waitForPageToLoad();
        historyPage.waitFor();
        assertEquals(ProjectHistoryDataAction.STATE_ANY, historyPage.getStateFilter());
        assertEmptyHistory(historyPage);
    }

    public void testSingleBuild() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random);
        int buildNumber = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);
        getBrowser().loginAsAdmin();

        ProjectHistoryPage historyPage = getBrowser().openAndWaitFor(ProjectHistoryPage.class, random);
        BuildInfo build = new BuildInfo(buildNumber, ResultState.SUCCESS, xmlRpcHelper.getBuildRevision(random, buildNumber));
        assertSingleBuildHistory(historyPage, build);

        setFilterAndWait(historyPage, ProjectHistoryDataAction.STATE_BROKEN);
        assertEmptyHistory(historyPage);

        setFilterAndWait(historyPage, ProjectHistoryDataAction.STATE_SUCCESS);
        assertSingleBuildHistory(historyPage, build);
    }
    
    public void testMultiplePages() throws Exception
    {
        final int BUILD_COUNT = 13;
        final int INDEX_FAILED_BUILD = 4;
        final int INDEX_ERROR_BUILD = 8;
        
        String projectPath = xmlRpcHelper.insertSimpleProject(random);
        String antCommandPath = PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND);
        
        Hashtable<String, Object> originalAntConfig = xmlRpcHelper.getConfig(antCommandPath);
        originalAntConfig.put(TARGETS, "");
        originalAntConfig.put("exe", "");
        
        List<BuildInfo> builds = new LinkedList<BuildInfo>();
        for (int i = 0; i < BUILD_COUNT; i++)
        {
            ResultState expectedState = ResultState.SUCCESS;
            if (i == INDEX_FAILED_BUILD)
            {
                Hashtable<String, Object> antConfig = xmlRpcHelper.getConfig(antCommandPath);
                antConfig.put(TARGETS, "unknown");
                xmlRpcHelper.saveConfig(antCommandPath, antConfig, false);
                expectedState = ResultState.FAILURE;
            }
            else if (i == INDEX_ERROR_BUILD)
            {
                Hashtable<String, Object> antConfig = xmlRpcHelper.getConfig(antCommandPath);
                antConfig.put("exe", "nosuchantcomment");
                xmlRpcHelper.saveConfig(antCommandPath, antConfig, false);
                expectedState = ResultState.ERROR;
            }

            int buildNumber = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);
            builds.add(0, new BuildInfo(buildNumber, expectedState, xmlRpcHelper.getBuildRevision(random, buildNumber)));
            
            if (expectedState != ResultState.SUCCESS)
            {
                xmlRpcHelper.saveConfig(antCommandPath, originalAntConfig, false);
            }
        }

        
        getBrowser().loginAsAdmin();

        ProjectHistoryPage historyPage = getBrowser().openAndWaitFor(ProjectHistoryPage.class, random);
        assertEquals(ProjectHistoryDataAction.BUILDS_PER_PAGE, historyPage.getBuildCount());
        assertFirstPage(historyPage, BUILD_COUNT);
        assertEquals(builds.subList(0, ProjectHistoryDataAction.BUILDS_PER_PAGE), historyPage.getBuilds());
        
        // Step forward
        historyPage.getPager().clickNext();
        getBrowser().waitForPageToLoad();
        historyPage.waitFor();

        assertEquals(BUILD_COUNT - ProjectHistoryDataAction.BUILDS_PER_PAGE, historyPage.getBuildCount());
        assertLastPage(historyPage, 2, BUILD_COUNT);
        assertEquals(builds.subList(ProjectHistoryDataAction.BUILDS_PER_PAGE, builds.size()), historyPage.getBuilds());

        // Step backward
        historyPage.getPager().clickFirst();
        getBrowser().waitForPageToLoad();
        historyPage.waitFor();

        assertEquals(ProjectHistoryDataAction.BUILDS_PER_PAGE, historyPage.getBuildCount());
        assertFirstPage(historyPage, BUILD_COUNT);
        assertEquals(builds.subList(0, ProjectHistoryDataAction.BUILDS_PER_PAGE), historyPage.getBuilds());
        
        // Filter to broken
        setFilterAndWait(historyPage, ProjectHistoryDataAction.STATE_BROKEN);
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
        setFilterAndWait(historyPage, ProjectHistoryDataAction.STATE_SUCCESS);
        List<BuildInfo> successfulBuilds = CollectionUtils.filter(builds, new Predicate<BuildInfo>()
        {
            public boolean satisfied(BuildInfo buildInfo)
            {
                return buildInfo.status == ResultState.SUCCESS;
            }
        });
        
        assertEquals(ProjectHistoryDataAction.BUILDS_PER_PAGE, historyPage.getBuildCount());
        assertFirstPage(historyPage, successfulBuilds.size());
        assertEquals(successfulBuilds.subList(0, ProjectHistoryDataAction.BUILDS_PER_PAGE), historyPage.getBuilds());
        
        // Step forward, ensure the filter is maintained.
        historyPage.getPager().clickLast();
        getBrowser().waitForPageToLoad();
        historyPage.waitFor();

        assertEquals(successfulBuilds.size() - ProjectHistoryDataAction.BUILDS_PER_PAGE, historyPage.getBuildCount());
        assertLastPage(historyPage, 2, successfulBuilds.size());
        assertEquals(successfulBuilds.subList(ProjectHistoryDataAction.BUILDS_PER_PAGE, successfulBuilds.size()), historyPage.getBuilds());
    }

    private void setFilterAndWait(ProjectHistoryPage historyPage, String stateFilter)
    {
        historyPage.setStateFilter(stateFilter);
        getBrowser().waitForPageToLoad();
        historyPage.waitFor();
    }

    private void assertEmptyHistory(ProjectHistoryPage historyPage)
    {
        assertEquals(0, historyPage.getBuildCount());
        assertNoPaging(historyPage, 0);
    }

    private void assertSingleBuildHistory(ProjectHistoryPage historyPage, BuildInfo build)
    {
        assertEquals(1, historyPage.getBuildCount());
        assertNoPaging(historyPage, 1);
        assertEquals(asList(build), historyPage.getBuilds());
    }

    private void assertNoPaging(ProjectHistoryPage historyPage, int totalItems)
    {
        Pager pager = historyPage.getPager();
        assertEquals(0, pager.getCurrentPage());
        assertEquals(totalItems, pager.getTotalItems());
        assertFalse(pager.hasPagingRow());
    }

    private void assertFirstPage(ProjectHistoryPage historyPage, int totalItems)
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

    private void assertLastPage(ProjectHistoryPage historyPage, int totalPages, int totalItems)
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
