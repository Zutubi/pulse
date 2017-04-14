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

import com.zutubi.pulse.acceptance.pages.agents.AgentHistoryPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildInfo;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.xwork.actions.ajax.HistoryDataAction;
import com.zutubi.tove.type.record.PathUtils;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Acceptance tests for the agent history tab.
 */
public class AgentHistoryAcceptanceTest extends HistoryAcceptanceTestBase
{
    public void setUp() throws Exception
    {
        super.setUp();

        rpcClient.TestApi.cancelActiveBuilds();
        rpcClient.RemoteApi.deleteAllConfigs(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    public void testNoBuilds() throws Exception
    {
        getBrowser().loginAsAdmin();

        String agentPath = rpcClient.RemoteApi.insertLocalAgent(random);
        try
        {
            AgentHistoryPage historyPage = getBrowser().openAndWaitFor(AgentHistoryPage.class, random);
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
        finally
        {
            rpcClient.RemoteApi.deleteConfig(agentPath);
        }
    }

    public void testMultipleProjects() throws Exception
    {
        String agentPath = rpcClient.RemoteApi.insertLocalAgent(random);
        try
        {
            String project1 = random + "-1";
            String project2 = random + "-2";

            rpcClient.RemoteApi.insertSimpleProject(project1);
            assignStageToAgent(project1, "default", random);
            rpcClient.RemoteApi.insertSimpleProject(project2);
            assignStageToAgent(project2, "default", random);
            rpcClient.RemoteApi.runBuild(project1, BUILD_TIMEOUT);
            rpcClient.RemoteApi.runBuild(project1, BUILD_TIMEOUT);
            rpcClient.RemoteApi.runBuild(project2, BUILD_TIMEOUT);

            getBrowser().loginAsAdmin();

            AgentHistoryPage historyPage = getBrowser().openAndWaitFor(AgentHistoryPage.class, random);
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
        finally
        {
            rpcClient.RemoteApi.deleteConfig(agentPath);
        }
    }
}
