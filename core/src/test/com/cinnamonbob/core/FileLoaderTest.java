package com.cinnamonbob.core;

import com.cinnamonbob.test.BobTestCase;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class FileLoaderTest extends BobTestCase
{

    private FileLoader loader;

    public FileLoaderTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        ObjectFactory factory = new ObjectFactory();
        loader = new FileLoader();
        loader.setObjectFactory(factory);

        // initialise the loader some test objects.
        loader.register("reference", SimpleReference.class);
        loader.register("nested", SimpleNestedType.class);
        loader.register("type", SimpleType.class);
        loader.register("some-reference", SomeReference.class);

        // initialise the loader with some real objects.
        loader.register("property", Property.class);
        loader.register("recipe", Recipe.class);
        loader.register("def", ComponentDefinition.class);
        loader.register("post-processor", PostProcessorGroup.class);
        loader.register("command", CommandGroup.class);
        loader.register("regex", RegexPostProcessor.class);
        loader.register("executable", ExecutableCommand.class);
        loader.register("dependency", Dependency.class);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

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
        assertEquals("a", ((SomeReference)a).getSomeValue());

    }

    public void testSampleProject() throws Exception
    {
        BobFile bf = new BobFile();
        List<Reference> properties = new LinkedList<Reference>();
        Property property = new Property("work.dir", "/whatever");
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

        ExecutableCommand command = (ExecutableCommand)commands.get(commandIndex);
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
}
