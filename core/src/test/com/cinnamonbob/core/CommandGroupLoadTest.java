package com.cinnamonbob.core;

/**
 */
public class CommandGroupLoadTest extends FileLoaderTestBase
{
    public void testBasicCommandGroup() throws Exception
    {
        CommandGroup group = loadGroup("basic");
        assertEquals("basic", group.getName());
        assertTrue(group.getCommand() instanceof ExecutableCommand);
        assertEquals(2, group.getArtifacts().size());
        Artifact a = group.getArtifacts().get(0);
        assertTrue(a instanceof FileArtifact);
        a = group.getArtifacts().get(1);
        assertTrue(a instanceof DirectoryArtifact);
    }

    private CommandGroup loadGroup(String name) throws BobException
    {
        BobFile bf = load(name);
        assertEquals(1, bf.getRecipes().size());
        Recipe recipe = bf.getRecipes().get(0);
        assertEquals(1, recipe.getCommands().size());
        Command command = recipe.getCommands().get(0);
        assertTrue(command instanceof CommandGroup);
        return (CommandGroup) command;
    }
}
