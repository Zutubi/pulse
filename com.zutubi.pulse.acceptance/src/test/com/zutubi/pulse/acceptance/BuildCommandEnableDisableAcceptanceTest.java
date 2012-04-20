package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.utils.BuildRunner;
import com.zutubi.pulse.acceptance.utils.ProjectConfigurationHelper;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.core.SleepCommandConfiguration;
import com.zutubi.pulse.core.engine.api.ResultState;
import static com.zutubi.pulse.core.engine.api.ResultState.*;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard.*;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Hashtable;
import java.util.Vector;

public class BuildCommandEnableDisableAcceptanceTest extends AcceptanceTestBase
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
    public void testEnabledCommands() throws Exception
    {
        ProjectConfigurationHelper project = projectConfigurations.createTrivialAntProject(randomName());
        project.addCommand(newCommand("sleep1"));
        project.addCommand(newCommand("sleep2"));

        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        // check the output of the build.
        Vector<Hashtable<String, Object>> commands = getCommands(project.getName(), DEFAULT_STAGE, 1);
        assertCommandState(commands, "sleep1", SUCCESS);
        assertCommandState(commands, "sleep2", SUCCESS);
        assertCommandState(commands, DEFAULT_COMMAND, SUCCESS);

        assertEquals(SUCCESS, rpcClient.RemoteApi.getBuildStatus(project.getName(), 1));
    }

    public void testDisableCommand() throws Exception
    {
        ProjectConfigurationHelper project = projectConfigurations.createTrivialAntProject(randomName());
        project.addCommand(newCommand("sleep")).setEnabled(false);

        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        // check the output of the build.
        Vector<Hashtable<String, Object>> commands = getCommands(project.getName(), DEFAULT_STAGE, 1);
        assertCommandState(commands, "sleep", SKIPPED);
        assertCommandState(commands, DEFAULT_COMMAND, SUCCESS);

        assertEquals(SUCCESS, rpcClient.RemoteApi.getBuildStatus(project.getName(), 1));
    }

    public void testDisableCommandInFailingBuild() throws Exception
    {
        ProjectConfigurationHelper project = projectConfigurations.createFailAntProject(randomName());
        project.addCommand(newCommand("sleep", true)).setEnabled(false);
        project.getDefaultCommand().setForce(true);

        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        Vector<Hashtable<String, Object>> commands = getCommands(project.getName(), DEFAULT_STAGE, 1);
        assertCommandState(commands, "sleep", SKIPPED);
        assertCommandState(commands, DEFAULT_COMMAND, FAILURE);

        assertEquals(FAILURE, rpcClient.RemoteApi.getBuildStatus(project.getName(), 1));
    }

    public void testDisableMultipleCommands() throws Exception
    {
        ProjectConfigurationHelper project = projectConfigurations.createTrivialAntProject(randomName());
        project.addCommand(newCommand("sleep")).setEnabled(false);
        project.getDefaultCommand().setEnabled(false);

        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        // check the output of the build.
        Vector<Hashtable<String, Object>> commands = getCommands(project.getName(), DEFAULT_STAGE, 1);
        assertCommandState(commands, "sleep", SKIPPED);
        assertCommandState(commands, DEFAULT_COMMAND, SKIPPED);

        assertEquals(SUCCESS, rpcClient.RemoteApi.getBuildStatus(project.getName(), 1));
    }

    public void testToggleEnableDisable() throws Exception
    {
        ProjectConfigurationHelper project = projectConfigurations.createTrivialAntProject(randomName());
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        assertEquals(SUCCESS, rpcClient.RemoteApi.getBuildStatus(project.getName(), 1));

        Vector<Hashtable<String, Object>> commands = getCommands(project.getName(), DEFAULT_STAGE, 1);
        assertCommandState(commands, DEFAULT_COMMAND, SUCCESS);

        toggle(project.getName(), DEFAULT_RECIPE, DEFAULT_COMMAND);

        buildRunner.triggerAndWaitForBuild(project);

        assertEquals(SUCCESS, rpcClient.RemoteApi.getBuildStatus(project.getName(), 2));
        commands = getCommands(project.getName(), DEFAULT_STAGE, 2);
        assertCommandState(commands, DEFAULT_COMMAND, SKIPPED);
    }

    private void toggle(String project, String recipe, String command) throws Exception
    {
        String path = "projects/" + project + "/type/recipes/" + recipe + "/commands/" + command;
        Hashtable<String, Object> config = rpcClient.RemoteApi.getConfig(path);
        config.put("enabled", !(Boolean)config.get("enabled"));
        rpcClient.RemoteApi.saveConfig(path, config, false);
    }

    private Vector<Hashtable<String, Object>> getCommands(String project, final String stage, int build) throws Exception
    {
        Hashtable<String, Object> buildDetails = rpcClient.RemoteApi.getBuild(project, build);
        Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) buildDetails.get("stages");
        Hashtable<String, Object> stageDetails = CollectionUtils.find(stages, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> details)
            {
                return details.get("name").equals(stage);
            }
        });
        return (Vector<Hashtable<String, Object>>) stageDetails.get("commands");
    }

    private CommandConfiguration newCommand(String name)
    {
        return newCommand(name, false);
    }

    private CommandConfiguration newCommand(String name, boolean force)
    {
        CommandConfiguration command = new SleepCommandConfiguration(name);
        command.setForce(force);
        return command;
    }

    private void assertCommandState(Vector<Hashtable<String, Object>> commands, String name, ResultState state)
    {
        boolean asserted = false;
        for (Hashtable<String, Object> command : commands)
        {
            if (command.get("name").equals(name))
            {
                assertEquals(state, ResultState.fromPrettyString((String) command.get("status")));
                asserted = true;
            }
        }
        assertTrue(asserted);
    }
}
