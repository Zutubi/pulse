package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Hashtable;
import java.util.Vector;

import static com.zutubi.pulse.core.engine.api.ResultState.*;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard.DEFAULT_STAGE;

public class BuildStageEnableDisableAcceptanceTest extends AcceptanceTestBase
{
    private ProjectConfigurations projects;
    private ConfigurationHelper configurationHelper;
    private BuildRunner buildRunner;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(xmlRpcHelper);

        projects = new ProjectConfigurations(configurationHelper);
        buildRunner = new BuildRunner(xmlRpcHelper);
        xmlRpcHelper.loginAsAdmin();
    }

    // base line test.
    public void testEnabledStages() throws Exception
    {
        ProjectConfigurationHelper project = projects.createTrivialAntProject(randomName());
        project.addStage("stage1");
        project.addStage("stage2");

        configurationHelper.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        // check the output of the build.
        Hashtable<String, Object> build = xmlRpcHelper.getBuild(project.getName(), 1);
        assertStageState(build, "stage1", SUCCESS);
        assertStageState(build, "stage2", SUCCESS);
        assertStageState(build, DEFAULT_STAGE, SUCCESS);

        assertEquals(SUCCESS, xmlRpcHelper.getBuildStatus(project.getName(), 1));
    }

    public void testDisableStage() throws Exception
    {
        ProjectConfigurationHelper project = projects.createTrivialAntProject(randomName());
        project.addStage("disabled").setEnabled(false);

        configurationHelper.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        // check the output of the build.
        Hashtable<String, Object> build = xmlRpcHelper.getBuild(project.getName(), 1);
        assertStageState(build, "disabled", SKIPPED);
        assertStageState(build, DEFAULT_STAGE, SUCCESS);

        assertEquals(SUCCESS, xmlRpcHelper.getBuildStatus(project.getName(), 1));
    }

    public void testDisableStageInFailingBuild() throws Exception
    {
        ProjectConfigurationHelper project = projects.createFailAntProject(randomName());
        project.addStage("disabled").setEnabled(false);

        configurationHelper.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        Hashtable<String, Object> build = xmlRpcHelper.getBuild(project.getName(), 1);
        assertStageState(build, "disabled", SKIPPED);
        assertStageState(build, DEFAULT_STAGE, FAILURE);

        assertEquals(FAILURE, xmlRpcHelper.getBuildStatus(project.getName(), 1));
    }

    public void testDisableMultipleStages() throws Exception
    {
        ProjectConfigurationHelper project = projects.createTrivialAntProject(randomName());
        project.addStage("disabled").setEnabled(false);
        project.getDefaultStage().setEnabled(false);

        configurationHelper.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        Hashtable<String, Object> build = xmlRpcHelper.getBuild(project.getName(), 1);
        assertStageState(build, "disabled", SKIPPED);
        assertStageState(build, DEFAULT_STAGE, SKIPPED);

        assertEquals(SUCCESS, xmlRpcHelper.getBuildStatus(project.getName(), 1));
    }

    public void testToggleEnableDisable() throws Exception
    {
        ProjectConfigurationHelper project = projects.createTrivialAntProject(randomName());
        configurationHelper.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        assertEquals(SUCCESS, xmlRpcHelper.getBuildStatus(project.getName(), 1));
        
        Hashtable<String, Object> build = xmlRpcHelper.getBuild(project.getName(), 1);
        assertStageState(build, DEFAULT_STAGE, SUCCESS);

        toggle(project.getName(), DEFAULT_STAGE);

        buildRunner.triggerAndWaitForBuild(project);

        assertEquals(SUCCESS, xmlRpcHelper.getBuildStatus(project.getName(), 2));

        build = xmlRpcHelper.getBuild(project.getName(), 2);
        assertStageState(build, DEFAULT_STAGE, SKIPPED);
    }

    private void toggle(String projectName, String stageName) throws Exception
    {
        String stagePath = "projects/" + projectName + "/stages/" + stageName;

        Hashtable<String, Object> stage = xmlRpcHelper.getConfig(stagePath);
        stage.put("enabled", !(Boolean) stage.get("enabled"));
        xmlRpcHelper.saveConfig(stagePath, stage, false);
    }

    private void assertStageState(Hashtable<String, Object> build, final String stageName, ResultState state)
    {
        Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) build.get("stages");
        Hashtable<String, Object> stage = CollectionUtils.find(stages, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> stage)
            {
                return stage.get("name").equals(stageName);
            }
        });
        assertNotNull(stage);
        assertEquals(state, ResultState.fromPrettyString((String) stage.get("status")));
    }
}
