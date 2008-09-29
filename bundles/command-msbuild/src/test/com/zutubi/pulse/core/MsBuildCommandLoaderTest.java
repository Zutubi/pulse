package com.zutubi.pulse.core;

import java.util.List;

public class MsBuildCommandLoaderTest extends FileLoaderTestBase
{
    public void setUp() throws Exception
    {
        super.setUp();
        loader.register("msbuild", MsBuildCommand.class);
    }

    private MsBuildCommand msbuildCommandHelper(String commandName) throws Exception
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("commands"), bf);

        List<Recipe> recipes = bf.getRecipes();
        assertEquals(recipes.size(), 1);

        Recipe recipe = recipes.get(0);
        List<Command> commands = recipe.getCommands();
        for (Command command: commands)
        {
            if (command.getName().equals(commandName))
            {
                assertTrue(command instanceof MsBuildCommand);
                return (MsBuildCommand) command;
            }
        }

        fail("Command '" + commandName + "' not found");
        return null;
    }

    public void testDefaults() throws Exception
    {
        MsBuildCommand command = msbuildCommandHelper("defaults");
        assertNull(command.getExe());
        assertNull(command.getBuildFile());
        assertNull(command.getTargets());
        assertNull(command.getConfiguration());
        assertEquals(0, command.getBuildProperties().size());
        assertTrue(command.isPostProcess());
    }

    public void testBuildFile() throws Exception
    {
        MsBuildCommand command = msbuildCommandHelper("buildfile");
        assertEquals("mybf", command.getBuildFile());
    }

    public void testTargets() throws Exception
    {
        MsBuildCommand command = msbuildCommandHelper("targets");
        assertEquals("clean test", command.getTargets());
    }

    public void testConfiguration() throws Exception
    {
        MsBuildCommand command = msbuildCommandHelper("configuration");
        assertEquals("Release", command.getConfiguration());
    }

    public void testProperties() throws Exception
    {
        MsBuildCommand command = msbuildCommandHelper("properties");
        List<MsBuildCommand.BuildProperty> properties = command.getBuildProperties();
        assertEquals(1, properties.size());
        MsBuildCommand.BuildProperty buildProperty = properties.get(0);
        assertEquals("foo", buildProperty.getName());
        assertEquals("bar", buildProperty.getValue());
    }

    public void testPropertyValidation() throws Exception
    {
        PulseFile bf = new PulseFile();
        try
        {
            loader.load(getInput("propertyvalidation"), bf);
            fail("Should not be able to load a build-property with no name");
        }
        catch (PulseException e)
        {
            assertTrue(e.getMessage().contains("name not specified"));
        }
    }

    public void testNoPostProcess() throws Exception
    {
        MsBuildCommand command = msbuildCommandHelper("nopp");
        assertFalse(command.isPostProcess());
    }
}
