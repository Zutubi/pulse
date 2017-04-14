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
import com.zutubi.pulse.acceptance.utils.BuildRunner;
import com.zutubi.pulse.acceptance.utils.ProjectConfigurationHelper;
import com.zutubi.pulse.core.engine.api.ResultState;

import java.util.Hashtable;
import java.util.Vector;

import static com.google.common.collect.Iterables.find;
import static com.zutubi.pulse.core.engine.api.ResultState.*;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard.DEFAULT_STAGE;

public class BuildStageEnableDisableAcceptanceTest extends AcceptanceTestBase
{
    private BuildRunner buildRunner;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        buildRunner = new BuildRunner(rpcClient.RemoteApi);
        rpcClient.loginAsAdmin();
    }

    // base line test.
    public void testEnabledStages() throws Exception
    {
        ProjectConfigurationHelper project = projectConfigurations.createTrivialAntProject(randomName());
        project.addStage("stage1");
        project.addStage("stage2");

        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        // check the output of the build.
        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(project.getName(), 1);
        assertStageState(build, "stage1", SUCCESS);
        assertStageState(build, "stage2", SUCCESS);
        assertStageState(build, DEFAULT_STAGE, SUCCESS);

        assertEquals(SUCCESS, rpcClient.RemoteApi.getBuildStatus(project.getName(), 1));
    }

    public void testDisableStage() throws Exception
    {
        ProjectConfigurationHelper project = projectConfigurations.createTrivialAntProject(randomName());
        project.addStage("disabled").setEnabled(false);

        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        // check the output of the build.
        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(project.getName(), 1);
        assertStageState(build, "disabled", SKIPPED);
        assertStageState(build, DEFAULT_STAGE, SUCCESS);

        assertEquals(SUCCESS, rpcClient.RemoteApi.getBuildStatus(project.getName(), 1));
    }

    public void testDisableStageInFailingBuild() throws Exception
    {
        ProjectConfigurationHelper project = projectConfigurations.createFailAntProject(randomName());
        project.addStage("disabled").setEnabled(false);

        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(project.getName(), 1);
        assertStageState(build, "disabled", SKIPPED);
        assertStageState(build, DEFAULT_STAGE, FAILURE);

        assertEquals(FAILURE, rpcClient.RemoteApi.getBuildStatus(project.getName(), 1));
    }

    public void testDisableMultipleStages() throws Exception
    {
        ProjectConfigurationHelper project = projectConfigurations.createTrivialAntProject(randomName());
        project.addStage("disabled").setEnabled(false);
        project.getDefaultStage().setEnabled(false);

        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(project.getName(), 1);
        assertStageState(build, "disabled", SKIPPED);
        assertStageState(build, DEFAULT_STAGE, SKIPPED);

        assertEquals(SUCCESS, rpcClient.RemoteApi.getBuildStatus(project.getName(), 1));
    }

    public void testToggleEnableDisable() throws Exception
    {
        ProjectConfigurationHelper project = projectConfigurations.createTrivialAntProject(randomName());
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        assertEquals(SUCCESS, rpcClient.RemoteApi.getBuildStatus(project.getName(), 1));
        
        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(project.getName(), 1);
        assertStageState(build, DEFAULT_STAGE, SUCCESS);

        toggle(project.getName(), DEFAULT_STAGE);

        buildRunner.triggerAndWaitForBuild(project);

        assertEquals(SUCCESS, rpcClient.RemoteApi.getBuildStatus(project.getName(), 2));

        build = rpcClient.RemoteApi.getBuild(project.getName(), 2);
        assertStageState(build, DEFAULT_STAGE, SKIPPED);
    }

    private void toggle(String projectName, String stageName) throws Exception
    {
        String stagePath = "projects/" + projectName + "/stages/" + stageName;

        Hashtable<String, Object> stage = rpcClient.RemoteApi.getConfig(stagePath);
        stage.put("enabled", !(Boolean) stage.get("enabled"));
        rpcClient.RemoteApi.saveConfig(stagePath, stage, false);
    }

    private void assertStageState(Hashtable<String, Object> build, final String stageName, ResultState state)
    {
        Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) build.get("stages");
        Hashtable<String, Object> stage = find(stages, new Predicate<Hashtable<String, Object>>()
        {
            public boolean apply(Hashtable<String, Object> stage)
            {
                return stage.get("name").equals(stageName);
            }
        }, null);
        assertNotNull(stage);
        assertEquals(state, ResultState.fromPrettyString((String) stage.get("status")));
    }
}
