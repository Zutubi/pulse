package com.cinnamonbob.core;

import com.cinnamonbob.core.model.Property;
import com.cinnamonbob.core.util.SystemUtils;

import java.util.LinkedList;
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
        BobFile bf = new BobFile();
        List<Reference> properties = new LinkedList<Reference>();
        Property property = new Property("base.dir", "/whatever");
        properties.add(property);

        loader.load(getInput("testSampleProject"), bf, properties);
    }

    public void testDependency() throws Exception
    {
        BobFile bf = new BobFile();
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

    private List<ExecutableCommand.Arg> executableArgsHelper(int commandIndex) throws Exception
    {
        BobFile bf = new BobFile();
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
            BobFile bf = new BobFile();
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

    public void testProcessNoProcessor() throws BobException
    {
        try
        {
            BobFile bf = new BobFile();
            loader.load(getInput("testProcessNoProcessor"), bf);
            fail();
        }
        catch (ParseException e)
        {
            assertTrue(e.getMessage().contains("attribute 'processor' not specified"));
        }
    }

    public void testSpecificRecipe() throws BobException
    {
        BobFile bf = new BobFile();
        loader.setPredicate(new RecipeLoadPredicate(bf, "default"));
        loader.load(getInput("testSpecificRecipe"), bf);
        assertEquals(2, bf.getRecipes().size());
        assertNotNull(bf.getRecipe("default"));
        assertNotNull(bf.getRecipe("default").getCommand("build"));
        assertNotNull(bf.getRecipe("don't load!"));
    }

    public void testSpecificRecipeDefault() throws BobException
    {
        BobFile bf = new BobFile();
        loader.setPredicate(new RecipeLoadPredicate(bf, null));
        loader.load(getInput("testSpecificRecipe"), bf);
        assertEquals(2, bf.getRecipes().size());
        assertNotNull(bf.getRecipe("default"));
        assertNotNull(bf.getRecipe("default").getCommand("build"));
        assertNotNull(bf.getRecipe("don't load!"));
    }

    public void testSpecificRecipeError() throws BobException
    {
        try
        {
            BobFile bf = new BobFile();
            loader.setPredicate(new RecipeLoadPredicate(bf, "don't load!"));
            loader.load(getInput("testSpecificRecipe"), bf);
            fail();
        }
        catch (BobException e)
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
        catch (BobException e)
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
        BobFile bf = new BobFile();
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
        assertEquals(SystemUtils.isWindows() ? "ant.bat" : "ant", command.getExe());
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
        BobFile bf = new BobFile();
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
