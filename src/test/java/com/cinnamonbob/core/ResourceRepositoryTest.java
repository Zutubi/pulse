package com.cinnamonbob.core;

import com.cinnamonbob.test.BobTestCase;

import java.util.List;
import java.util.Map;

/**
 * <class-comment/>
 */
public class ResourceRepositoryTest extends BobTestCase
{
    private ResourceRepository repo = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        repo = new ResourceRepository();
        FileLoader loader = new FileLoader();
        loader.setObjectFactory(new ObjectFactory());
        repo.setFileLoader(loader);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testEmptyRepo() throws Exception
    {
        repo.load(getInput("testEmptyRepo"));
        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(0, resources.size());
    }

    public void testResource() throws Exception
    {
        repo.load(getInput("testResource"));
        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(1, resources.size());

        Resource resource = repo.getResource("aResource");
        assertNotNull(resource);
        Map<String, Property> props = resource.getProperties();
        assertEquals(1, props.size());
        assertEquals("b", props.get("a").getValue());
    }

    public void testResourceWithVersion() throws Exception
    {
        repo.load(getInput("testResourceWithVersion"));
        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(1, resources.size());

        Resource resource = repo.getResource("aResource");
        assertNotNull(resource);

        ResourceVersion version = resource.getVersion("aVersion");
        assertNotNull(version);

        Map<String, Property> props = version.getProperties();
        assertEquals(2, props.size());
        assertEquals("c", props.get("b").getValue());
        assertEquals("e", props.get("d").getValue());
    }

    public void testMultipleResources() throws Exception
    {
        repo.load(getInput("testMultipleResources"));

        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(2, resources.size());

        Resource resource = repo.getResource("aResource");
        assertNotNull(resource);

        resource = repo.getResource("bResource");
        assertNotNull(resource);
        assertNotNull(resource.getVersion("aVersion"));
        assertNotNull(resource.getVersion("bVersion"));
    }
}
