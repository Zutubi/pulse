package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.core.commands.core.SleepCommandConfiguration;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.api.CommandConfigurationSupportActions;
import com.zutubi.pulse.core.engine.api.ResultState;

import java.util.Hashtable;
import java.util.Vector;

public class BuildCommandEnableDisableAcceptanceTest extends BaseXmlRpcAcceptanceTest
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
    public void testEnabledCommands() throws Exception
    {
        ProjectConfigurationHelper project = projects.createTrivialAntProject(randomName());
        project.addCommand(newCommand("sleep1"));
        project.addCommand(newCommand("sleep2"));

        configurationHelper.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        // check the output of the build.
        Vector<Hashtable<String, Object>> commands = xmlRpcHelper.getCommands(project.getName(), 1);
        assertCommandState(commands, "sleep1", "success");
        assertCommandState(commands, "sleep2", "success");
        assertCommandState(commands, "build", "success");

        assertEquals(ResultState.SUCCESS, xmlRpcHelper.getBuildStatus(project.getName(), 1));
    }

    public void testDisableCommand() throws Exception
    {
        ProjectConfigurationHelper project = projects.createTrivialAntProject(randomName());
        project.addCommand(newCommand("sleep")).setEnabled(false);

        configurationHelper.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        // check the output of the build.
        Vector<Hashtable<String, Object>> commands = xmlRpcHelper.getCommands(project.getName(), 1);
        assertCommandState(commands, "sleep", "skipped");
        assertCommandState(commands, "build", "success");

        assertEquals(ResultState.SUCCESS, xmlRpcHelper.getBuildStatus(project.getName(), 1));
    }

    public void testDisableCommandInFailingBuild() throws Exception
    {
        ProjectConfigurationHelper project = projects.createFailAntProject(randomName());
        project.addCommand(newCommand("sleep", true)).setEnabled(false);
        project.getDefaultCommand().setForce(true);

        configurationHelper.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        Vector<Hashtable<String, Object>> commands = xmlRpcHelper.getCommands(project.getName(), 1);
        assertCommandState(commands, "sleep", "skipped");
        assertCommandState(commands, "build", "failure");

        assertEquals(ResultState.FAILURE, xmlRpcHelper.getBuildStatus(project.getName(), 1));
    }

    public void testDisableMultipleCommands() throws Exception
    {
        ProjectConfigurationHelper project = projects.createTrivialAntProject(randomName());
        project.addCommand(newCommand("sleep")).setEnabled(false);
        project.getDefaultCommand().setEnabled(false);

        configurationHelper.insertProject(project.getConfig(), false);
        buildRunner.triggerAndWaitForBuild(project);

        // check the output of the build.
        Vector<Hashtable<String, Object>> commands = xmlRpcHelper.getCommands(project.getName(), 1);
        assertCommandState(commands, "sleep", "skipped");
        assertCommandState(commands, "build", "skipped");

        assertEquals(ResultState.SUCCESS, xmlRpcHelper.getBuildStatus(project.getName(), 1));
    }

    public void testEnableDisableViaRemoteApi() throws Exception
    {
        ProjectConfigurationHelper project = projects.createTrivialAntProject(randomName());
        project.getDefaultCommand().setEnabled(false);

        configurationHelper.insertProject(project.getConfig(), false);
        String commandPath = "projects/" + project.getName() + "/type/recipes/" + project.getDefaultRecipe().getName() + "/commands/" + project.getDefaultCommand().getName();

        Vector<String> actions = xmlRpcHelper.getConfigActions(commandPath);
        assertTrue(actions.contains(CommandConfigurationSupportActions.ACTION_ENABLE));
        assertFalse(actions.contains(CommandConfigurationSupportActions.ACTION_DISABLE));

        xmlRpcHelper.doConfigAction(commandPath, CommandConfigurationSupportActions.ACTION_ENABLE);

        actions = xmlRpcHelper.getConfigActions(commandPath);
        assertFalse(actions.contains(CommandConfigurationSupportActions.ACTION_ENABLE));
        assertTrue(actions.contains(CommandConfigurationSupportActions.ACTION_DISABLE));
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

    private void assertCommandState(Vector<Hashtable<String, Object>> commands, String name, String state)
    {
        boolean asserted = false;
        for (Hashtable<String, Object> command : commands)
        {
            if (command.get("name").equals(name))
            {
                assertEquals(state, command.get("state"));
                asserted = true;
            }
        }
        assertTrue(asserted);
    }
}
