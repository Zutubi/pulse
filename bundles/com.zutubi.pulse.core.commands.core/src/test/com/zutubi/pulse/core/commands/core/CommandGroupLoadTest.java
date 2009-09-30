package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.api.PulseException;

/**
 */
public class CommandGroupLoadTest extends FileLoaderTestBase
{
    public void setUp() throws Exception
    {
        super.setUp();

        loader.register("command", CommandGroup.class);
        loader.register("executable", ExecutableCommand.class);
    }

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
        LocalArtifact a = (LocalArtifact) group.getArtifacts().get(0);
        assertTrue(a instanceof FileArtifact);
        assertFalse(a.getFailIfNotPresent());
        a = (LocalArtifact) group.getArtifacts().get(1);
        assertTrue(a instanceof DirectoryArtifact);
        assertFalse(a.getFailIfNotPresent());
    }

    public void testLinkageCommandGroup() throws Exception
    {
        CommandGroup group = loadGroup("basic", "linkage");
        assertEquals(1, group.getArtifacts().size());
        Artifact a = group.getArtifacts().get(0);
        assertTrue(a instanceof LinkArtifact);
        LinkArtifact link = (LinkArtifact) a;
        assertEquals("link", link.getName());
        assertEquals("http://my/url", link.getUrl());
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

    public void testMissingLinkNameValidation()
    {
        try
        {
            loadGroup("linkname-validation", null);
            fail();
        }
        catch (PulseException e)
        {
            assertTrue(e.getMessage().contains("required attribute name not specified"));
        }
    }

    public void testMissingLinkUrlValidation()
    {
        try
        {
            loadGroup("linkurl-validation", null);
            fail();
        }
        catch (PulseException e)
        {
            assertTrue(e.getMessage().contains("Required attribute url not specified"));
        }
    }

    public void testNestedForce() throws Exception
    {
        CommandGroup group = loadGroup("basic", "nested force");
        assertTrue(group.isForce());
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
