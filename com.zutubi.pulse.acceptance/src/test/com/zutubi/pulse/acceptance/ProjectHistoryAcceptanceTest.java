/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.filter;
import static com.zutubi.pulse.acceptance.Constants.Project.AntCommand.TARGETS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.RECIPES;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.COMMANDS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND;
import static com.zutubi.pulse.acceptance.Constants.Project.TYPE;
import com.zutubi.pulse.acceptance.pages.browse.BuildInfo;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHistoryPage;
import com.zutubi.pulse.core.engine.api.ResultState;
import static com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration.DEFAULT_HISTORY_BUILDS_PER_PAGE;
import com.zutubi.pulse.master.xwork.actions.ajax.HistoryDataAction;
import com.zutubi.tove.type.record.PathUtils;
import org.python.google.common.collect.Lists;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

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
        List<BuildInfo> brokenBuilds = Lists.newArrayList(filter(builds, new Predicate<BuildInfo>()
        {
            public boolean apply(BuildInfo buildInfo)
            {
                return buildInfo.status != ResultState.SUCCESS;
            }
        }));
        
        assertEquals(brokenBuilds.size(), historyPage.getBuildCount());
        assertNoPaging(historyPage, 2);
        assertEquals(brokenBuilds, historyPage.getBuilds());
        
        // Filter to successful (still multiple pages)
        setFilterAndWait(historyPage, HistoryDataAction.STATE_SUCCESS);
        List<BuildInfo> successfulBuilds = Lists.newArrayList(filter(builds, new Predicate<BuildInfo>()
        {
            public boolean apply(BuildInfo buildInfo)
            {
                return buildInfo.status == ResultState.SUCCESS;
            }
        }));
        
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
