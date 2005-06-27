package com.cinnamonbob.core.config;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class ProjectConfigurationLoaderTest extends TestCase
{
    
    private ProjectConfigurationLoader loader;
    
    public ProjectConfigurationLoaderTest(String testName)
    {
        super(testName);
    }
    
    public void setUp() throws Exception
    {
        super.setUp();
        
        loader = new ProjectConfigurationLoader();
        
        // initialise the loader some test objects.
        loader.register("simpleTrigger", SimpleTrigger.class);
        loader.register("reference", SimpleReference.class);
        loader.register("nested", SimpleNestedType.class);
        loader.register("type", SimpleType.class);
        loader.register("some-reference", SomeReference.class);
        
        // initialise the loader with some real objects.
        loader.register("property", Property.class);
        loader.register("description", Description.class);
        loader.register("recipe", Recipe.class);
        loader.register("schedule", Schedule.class);
        loader.register("def", ComponentDefinition.class);
        loader.register("post-processor", PostProcessorGroup.class);
        loader.register("command", CommandGroup.class);
        loader.register("cron", CronTrigger.class);
        loader.register("regex", RegexPostProcessor.class);
        loader.register("executable", ExecutableCommand.class);
    }
    
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void testSimpleReference() throws Exception
    {
        Project project = loader.load(getClass().getResourceAsStream("testSimpleReference.xml"));
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
        Project project = loader.load(getClass().getResourceAsStream("testResolveReference.xml"));
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
        Project project = loader.load(getClass().getResourceAsStream("testNestedType.xml"));
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
        Project project = loader.load(getClass().getResourceAsStream("testInitComponent.xml"));
        assertNotNull(project);
        
        assertEquals("valueA", project.getProperty("a"));        
        assertEquals("valueB", project.getProperty("b"));        
    }
 
    public void testNonBeanName() throws Exception
    {
        Project project = loader.load(getClass().getResourceAsStream("testNonBeanName.xml"));
        assertNotNull(project);
        
        Object a = project.getReference("a");
        assertNotNull(a);
        assertTrue(a instanceof SomeReference);
        assertEquals("a", ((SomeReference)a).getSomeValue());
        
    }
    
    public void testSchedule() throws Exception
    {
        Project project = loader.load(getClass().getResourceAsStream("testSchedule.xml"));
        assertNotNull(project);
        
        Schedule a = project.getSchedule("a");
        assertNotNull(a);
        assertNotNull(a.getTriggers());
        assertEquals(1, a.getTriggers().size());
        
        Object o = a.getTriggers().get(0);
        assertTrue(o instanceof SimpleTrigger);
        assertEquals("b", ((SimpleTrigger)o).getName());
        assertEquals("c", ((SimpleTrigger)o).getValue());
    }
    
    public void testCreateProject() throws Exception
    {
        Project project = loader.load(getClass().getResourceAsStream("testCreateProject.xml"));
        assertNotNull(project);
        
        assertEquals("projectName", project.getName());
    }
    
    public void testSampleProject() throws Exception
    {
        Project project = loader.load(getClass().getResourceAsStream("testSampleProject.xml"));
        assertNotNull(project);
        
        assertEquals("sampleProject", project.getName());
    }
}
