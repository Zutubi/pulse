package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.util.SystemUtils;

import java.util.List;

/**
 * 
 *
 */
public class FileLoaderTest extends FileLoaderTestBase
{
    public void testSimpleReference() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("testSimpleReference"), root);

        Object o = root.getReference("a");
        assertNotNull(o);
        assertTrue(o instanceof SimpleReference);

        SimpleReference t = (SimpleReference) o;
        assertEquals("a", t.getName());
    }

    public void testResolveReference() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("testResolveReference"), root);

        Object a = root.getReference("a");
        assertNotNull(a);
        assertTrue(a instanceof SimpleReference);

        Object b = root.getReference("b");
        assertNotNull(b);
        assertTrue(b instanceof SimpleReference);

        SimpleReference rb = (SimpleReference) b;
        assertEquals(a, rb.getRef());
    }

    public void testNestedType() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("testNestedType"), root);

        assertNotNull(root.getReference("a"));

        SimpleNestedType a = (SimpleNestedType) root.getReference("a");
        assertNotNull(a.getNestedType("b"));
        assertNotNull(a.getNestedType("c"));
    }

    public void testNonBeanName() throws Exception
    {
        SimpleRoot root = new SimpleRoot();
        loader.load(getInput("testNonBeanName"), root);

        Object a = root.getReference("a");
        assertNotNull(a);
        assertTrue(a instanceof SomeReference);
        assertEquals("a", ((SomeReference) a).getSomeValue());

    }

    public void testSampleProject() throws Exception
    {
        PulseFile bf = new PulseFile();
        Scope scope = new Scope();
        Property property = new Property("base.dir", "/whatever");
        scope.add(property);

        loader.load(getInput("testSampleProject"), bf, scope, new FileResourceRepository(), null);
    }

    public void testDependency() throws Exception
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("testDependency"), bf);

        assertNotNull(bf.getDependencies());
        assertEquals(1, bf.getDependencies().size());
        assertEquals("1", bf.getDependencies().get(0).getName());
        assertEquals("2", bf.getDependencies().get(0).getVersion());

        Recipe recipe = bf.getRecipe(bf.getDefaultRecipe());
        assertNotNull(recipe);
        assertEquals(1, recipe.getDependencies().size());
        assertEquals("a", recipe.getDependencies().get(0).getName());
        assertEquals("b", recipe.getDependencies().get(0).getVersion());
    }

    public void testScoping() throws Exception
    {
        PulseFile pf = new PulseFile();
        loader.load(getInput("testScoping"), pf);

        Recipe recipe = pf.getRecipe("global");
        assertNotNull(recipe);
        Command command = recipe.getCommand("in recipe");
        assertNotNull(command);
        ExecutableCommand exe = (ExecutableCommand) ((CommandGroup)command).getCommand();
        assertEquals("in command", exe.getExe());
    }

    public void testMacro() throws Exception
    {
        PulseFile pf = new PulseFile();
        loader.load(getInput("testMacro"), pf);

        Recipe recipe = pf.getRecipe("r1");
        assertNotNull(recipe);
        Command command = recipe.getCommand("m1-e1");
        assertNotNull(command);
        command = recipe.getCommand("m1-e2");
        assertNotNull(command);
    }

    public void testMacroEmpty() throws Exception
    {
        PulseFile pf = new PulseFile();
        loader.load(getInput("testMacroEmpty"), pf);

        Recipe recipe = pf.getRecipe("r1");
        assertNotNull(recipe);
        assertEquals(0, recipe.getCommands().size());
    }

    public void testMacroExpandError() throws Exception
    {
        errorHelper("testMacroExpandError", "While expanding macro defined at line 4 column 5: Processing element 'no-such-type': starting at line 5 column 9: Undefined type 'no-such-type'");
    }

    public void testMacroNoName() throws Exception
    {
        errorHelper("testMacroNoName", "Required attribute 'name' not found");
    }

    public void testMacroUnknownAttribute() throws Exception
    {
        errorHelper("testMacroUnknownAttribute", "Unrecognised attribute 'unkat'");
    }

    public void testMacroRefNoMacro() throws Exception
    {
        errorHelper("testMacroRefNoMacro", "Required attribute 'macro' not found");
    }

    public void testMacroRefNotMacro() throws Exception
    {
        errorHelper("testMacroRefNotMacro", "Reference '${not-macro}' does not resolve to a macro");
    }

    public void testMacroRefNotFound() throws Exception
    {
        errorHelper("testMacroRefNotFound", "Unknown variable reference 'not-found'");
    }

    public void testMacroRefUnknownAttribute() throws Exception
    {
        errorHelper("testMacroRefUnknownAttribute", "Unrecognised attribute 'whatthe'");
    }

    public void testMacroInfiniteRecursion() throws Exception
    {
        errorHelper("testMacroInfiniteRecursion", "Maximum recursion depth exceeded");
    }

    private List<ExecutableCommand.Arg> executableArgsHelper(int commandIndex) throws Exception
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("testExecutableArgs"), bf);

        List<Recipe> recipes = bf.getRecipes();
        assertEquals(recipes.size(), 1);

        Recipe recipe = recipes.get(0);
        List<Command> commands = recipe.getCommands();
        assertEquals(commands.size(), 5);
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

    public void testValidation() throws Exception
    {
        try
        {
            PulseFile bf = new PulseFile();
            loader.load(getInput("testValidateable"), bf);
            fail();
        }
        catch (ParseException e)
        {
            assertEquals(e.getMessage(), "Processing element 'validateable': starting at line 4 column 5: error\n");
        }
    }

    public void testArtifactNameValidation() throws Exception
    {
        errorHelper("testArtifactNameValidation", "duplicate");
    }

    public void testArtifactInvalidName() throws Exception
    {
        errorHelper("testArtifactInvalidName", "alphanumeric");
    }

    public void testArtifactMissingName() throws Exception
    {
        errorHelper("testArtifactMissingName", "Required attribute name not specified");
    }

    public void testProcessNoProcessor() throws PulseException
    {
        try
        {
            PulseFile bf = new PulseFile();
            loader.load(getInput("testProcessNoProcessor"), bf);
            fail();
        }
        catch (ParseException e)
        {
            assertTrue(e.getMessage().contains("attribute 'processor' not specified"));
        }
    }

    public void testSpecificRecipe() throws PulseException
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("testSpecificRecipe"), bf, null, new FileResourceRepository(), new RecipeLoadPredicate(bf, "default"));
        assertEquals(2, bf.getRecipes().size());
        assertNotNull(bf.getRecipe("default"));
        assertNotNull(bf.getRecipe("default").getCommand("build"));
        assertNotNull(bf.getRecipe("don't load!"));
    }

    public void testSpecificRecipeDefault() throws PulseException
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("testSpecificRecipe"), bf, null, new FileResourceRepository(), new RecipeLoadPredicate(bf, null));
        assertEquals(2, bf.getRecipes().size());
        assertNotNull(bf.getRecipe("default"));
        assertNotNull(bf.getRecipe("default").getCommand("build"));
        assertNotNull(bf.getRecipe("don't load!"));
    }

    public void testSpecificRecipeError() throws PulseException
    {
        try
        {
            PulseFile bf = new PulseFile();
            loader.load(getInput("testSpecificRecipe"), bf, null, new FileResourceRepository(), new RecipeLoadPredicate(bf, "don't load!"));
            fail();
        }
        catch (PulseException e)
        {
            e.printStackTrace();
        }
    }

    public void testUnknownAttribute()
    {
        try
        {
            loader.load(getInput("testUnknownAttribute"), new SimpleType());
            fail();
        }
        catch (PulseException e)
        {
            if (!e.getMessage().contains("bad-attribute"))
            {
                fail();
            }
        }
    }

    //-----------------------------------------------------------------------
    // Ant command
    //-----------------------------------------------------------------------

    private AntCommand antCommandHelper(int commandIndex) throws Exception
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("testAntCommand"), bf);

        List<Recipe> recipes = bf.getRecipes();
        assertEquals(recipes.size(), 1);

        Recipe recipe = recipes.get(0);
        List<Command> commands = recipe.getCommands();
        assertTrue(commands.get(commandIndex) instanceof AntCommand);

        return (AntCommand) commands.get(commandIndex);
    }

    public void testAntCommandDefaults() throws Exception
    {
        AntCommand command = antCommandHelper(0);
        assertEquals(SystemUtils.IS_WINDOWS ? "ant.bat" : "ant", command.getExe());
        assertNull(command.getTargets());
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

    //-----------------------------------------------------------------------
    // Make command
    //-----------------------------------------------------------------------

    private MakeCommand makeCommandHelper(int commandIndex) throws Exception
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("testMakeCommand"), bf);

        List<Recipe> recipes = bf.getRecipes();
        assertEquals(recipes.size(), 1);

        Recipe recipe = recipes.get(0);
        List<Command> commands = recipe.getCommands();
        assertTrue(commands.get(commandIndex) instanceof MakeCommand);

        return (MakeCommand) commands.get(commandIndex);
    }

    public void testMakeCommandDefaults() throws Exception
    {
        MakeCommand command = makeCommandHelper(0);
        assertEquals("make", command.getExe());
        assertNull(command.getTargets());
    }

    public void testMakeCommandCustomExe() throws Exception
    {
        MakeCommand command = makeCommandHelper(1);
        assertEquals("mymake", command.getExe());
    }

    public void testMakeCommandTargets() throws Exception
    {
        MakeCommand command = makeCommandHelper(2);
        assertEquals("build test", command.getTargets());
    }

    public void testMakeCommandMakefile() throws Exception
    {
        MakeCommand command = makeCommandHelper(3);
        assertEquals("mymakefile", command.getMakefile());
    }
}
