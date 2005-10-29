package com.cinnamonbob.core;

import com.cinnamonbob.bootstrap.ComponentContext;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 *
 */
public class ProjectConfigurationLoaderTest extends TestCase
{

    private BobFileLoader loader;

    public ProjectConfigurationLoaderTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        String[] configLocations = new String[] {
            "com/cinnamonbob/bootstrap/emptyContext.xml"
        };

        ComponentContext.addClassPathContextDefinitions(configLocations);

        loader = new BobFileLoader();

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
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSimpleReference() throws Exception
    {
        BobFile project = loader.load(getClass().getResourceAsStream("testSimpleReference.xml"));
        assertNotNull(project);

        Object o = project.getReference("a");
        assertNotNull(o);
        assertTrue(o instanceof SimpleReference);

        SimpleReference t = (SimpleReference) o;
        assertEquals("a", t.getName());
        assertEquals(project, t.getProject());
    }

    public void testResolveReference() throws Exception
    {
        BobFile project = loader.load(getClass().getResourceAsStream("testResolveReference.xml"));
        assertNotNull(project);

        Object a = project.getReference("a");
        assertNotNull(a);
        assertTrue(a instanceof SimpleReference);

        Object b = project.getReference("b");
        assertNotNull(b);
        assertTrue(b instanceof SimpleReference);

        SimpleReference rb = (SimpleReference) b;
        assertEquals(a, rb.getRef());
    }

    public void testNestedType() throws Exception
    {
        BobFile project = loader.load(getClass().getResourceAsStream("testNestedType.xml"));
        assertNotNull(project);

        assertNotNull(project.getReference("a"));
        assertNotNull(project.getReference("b"));
        assertNotNull(project.getReference("c"));

        SimpleNestedType a = (SimpleNestedType) project.getReference("a");
        assertNotNull(a.getNestedType("b"));
        assertNotNull(a.getNestedType("c"));

        assertEquals(project.getReference("b"), a.getNestedType("b"));
        assertEquals(project.getReference("c"), a.getNestedType("c"));
    }

    public void testInitComponent() throws Exception
    {
        BobFile project = loader.load(getClass().getResourceAsStream("testInitComponent.xml"));
        assertNotNull(project);

        assertEquals("valueA", project.getProperty("a"));
        assertEquals("valueB", project.getProperty("b"));
    }

    public void testNonBeanName() throws Exception
    {
        BobFile project = loader.load(getClass().getResourceAsStream("testNonBeanName.xml"));
        assertNotNull(project);

        Object a = project.getReference("a");
        assertNotNull(a);
        assertTrue(a instanceof SomeReference);
        assertEquals("a", ((SomeReference)a).getSomeValue());

    }

    public void testCreateProject() throws Exception
    {
        BobFile project = loader.load(getClass().getResourceAsStream("testCreateProject.xml"));
        assertNotNull(project);
    }

    public void testSampleProject() throws Exception
    {
        Map<String, String> properties = new TreeMap<String, String>();
        properties.put("work.dir", ".");

        BobFile project = loader.load(getClass().getResourceAsStream("testSampleProject.xml"), properties);
        assertNotNull(project);
    }

    private List<ExecutableCommand.Arg> executableArgsHelper(int commandIndex) throws Exception
    {
        BobFile bf = loader.load(getClass().getResourceAsStream("testExecutableArgs.xml"));

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
