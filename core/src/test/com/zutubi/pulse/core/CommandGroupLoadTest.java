package com.zutubi.pulse.core;

/**
 */
public class CommandGroupLoadTest extends FileLoaderTestBase
{
    public void testBasicCommandGroup() throws Exception
    {
        CommandGroup group = loadGroup("basic", "basic");
        assertEquals("basic", group.getName());
        assertTrue(group.getCommand() instanceof ExecutableCommand);
        assertEquals(2, group.getArtifacts().size());
        Artifact a = group.getArtifacts().get(0);
        assertTrue(a instanceof FileArtifact);
        a = group.getArtifacts().get(1);
        assertTrue(a instanceof DirectoryArtifact);
    }

    public void testNoExistNoFail() throws Exception
    {
        CommandGroup group = loadGroup("basic", "noExistNoFail");
        assertEquals(2, group.getArtifacts().size());
        Artifact a = group.getArtifacts().get(0);
        assertTrue(a instanceof FileArtifact);
        assertFalse(a.getFailIfNotPresent());
        a = group.getArtifacts().get(1);
        assertTrue(a instanceof DirectoryArtifact);
        assertFalse(a.getFailIfNotPresent());
    }

    public void testMissingNestedCommandValidation()
    {
        try
        {
            loadGroup("command-validation", "noNestedCommand");
            fail();
        }
        catch (PulseException e)
        {
            assertTrue(e.getMessage().contains("The command tag requires a nested command."));
        }
    }

    public void testMissingNameCommandValidation()
    {
        try
        {
            loadGroup("name-validation", null);
            fail();
        }
        catch (PulseException e)
        {
            assertTrue(e.getMessage().contains("A name must be provided for the command (possibly on a surrounding 'command' tag)."));
        }
    }

    private CommandGroup loadGroup(String name, String commandName) throws PulseException
    {
        PulseFile bf = load(name);
        assertEquals(1, bf.getRecipes().size());
        Recipe recipe = bf.getRecipes().get(0);
        Command command = recipe.getCommand(commandName);
        assertNotNull(command);
        assertTrue(command instanceof CommandGroup);
        return (CommandGroup) command;
    }
}
