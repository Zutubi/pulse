package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.model.ResultState;

import java.util.List;

/**
 * <class comment/>
 */
public class ExecutableCommandLoaderTest extends FileLoaderTestBase
{
    public void setUp() throws Exception
    {
        super.setUp();

        loader.register("executable", ExecutableCommand.class);
        loader.register("property", Property.class);
    }

    private List<ExecutableCommand.Arg> executableArgsHelper(int commandIndex) throws Exception
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("testExecutableArgs"), bf);

        List<Recipe> recipes = bf.getRecipes();
        assertEquals(1, recipes.size());

        Recipe recipe = recipes.get(0);
        List<Command> commands = recipe.getCommands();
        assertEquals(6, commands.size());
        assertTrue(commands.get(commandIndex) instanceof ExecutableCommand);

        ExecutableCommand command = (ExecutableCommand) commands.get(commandIndex);
        return command.getArgs();
    }

    public void testExecutableArgsOne() throws Exception
    {
        List<ExecutableCommand.Arg> args = executableArgsHelper(0);
        assertEquals(args.size(), 1);
        assertEquals(args.get(0).getText(), "one");
    }

    public void testExecutableArgsOneTwo() throws Exception
    {
        List<ExecutableCommand.Arg> args = executableArgsHelper(1);
        assertEquals(args.size(), 2);
        assertEquals(args.get(0).getText(), "one");
        assertEquals(args.get(1).getText(), "two");
    }

    public void testExecutableArgsNested() throws Exception
    {
        List<ExecutableCommand.Arg> args = executableArgsHelper(2);
        assertEquals(args.size(), 1);
        assertEquals(args.get(0).getText(), "here are some spaces");
    }

    public void testExecutableArgsMultiNested() throws Exception
    {
        List<ExecutableCommand.Arg> args = executableArgsHelper(3);
        assertEquals(args.size(), 2);
        assertEquals(args.get(0).getText(), "here are some spaces");
        assertEquals(args.get(1).getText(), "and yet more spaces");
    }

    public void testExecutableArgsAttributeAndNested() throws Exception
    {
        List<ExecutableCommand.Arg> args = executableArgsHelper(4);
        assertEquals(args.size(), 4);
        assertEquals(args.get(0).getText(), "one");
        assertEquals(args.get(1).getText(), "two");
        assertEquals(args.get(2).getText(), "here are some spaces");
        assertEquals(args.get(3).getText(), "and yet more spaces");
    }

    public void testExecutableArgsVariableReferences() throws Exception
    {
        List<ExecutableCommand.Arg> args = executableArgsHelper(5);
        assertEquals(2, args.size());
        assertEquals("bar", args.get(0).getText());
        assertEquals("ref in text bar", args.get(1).getText());
    }

    public void testExecutableStatusMapping() throws PulseException
    {
        PulseFile pf = new PulseFile();
        loader.load(getInput(getName()), pf);

        Recipe recipe = pf.getRecipe("wow");
        ExecutableCommand command = (ExecutableCommand) recipe.getCommand("test");
        List<StatusMapping> statusMappings = command.getStatusMappings();
        assertEquals(1, statusMappings.size());
        StatusMapping mapping = statusMappings.get(0);
        assertEquals(2, mapping.getCode());
        assertEquals(ResultState.ERROR, mapping.getResultState());
    }
}
