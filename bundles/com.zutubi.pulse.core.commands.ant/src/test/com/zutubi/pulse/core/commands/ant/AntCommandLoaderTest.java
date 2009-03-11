package com.zutubi.pulse.core.commands.ant;

import com.zutubi.pulse.core.*;

import java.util.List;

/**
 * <class comment/>
 */
public class AntCommandLoaderTest extends FileLoaderTestBase
{
    public void setUp() throws Exception
    {
        super.setUp();

        loader.register("ant", AntCommand.class);
    }

    private AntCommand antCommandHelper(int commandIndex) throws Exception
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("testAntCommand", "xml"), bf, new ImportingNotSupportedFileResolver());

        List<Recipe> recipes = bf.getRecipes();
        assertEquals(recipes.size(), 1);

        Recipe recipe = recipes.get(0);
        List<Command> commands = recipe.getCommands();
        assertTrue(commands.get(commandIndex) instanceof AntCommand);

        return (AntCommand) commands.get(commandIndex);
    }

    public void testAntCommandCustomExe() throws Exception
    {
        AntCommand command = antCommandHelper(1);
        assertEquals("myant", command.getExe());
    }

    public void testAntCommandTargets() throws Exception
    {
        AntCommand command = antCommandHelper(2);
        assertEquals("build test", command.getTargets());
    }

    public void testAntCommandBuildFile() throws Exception
    {
        AntCommand command = antCommandHelper(3);
        assertEquals("mybuild.xml", command.getBuildFile());
    }
}
