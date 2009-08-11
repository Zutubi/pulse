package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.List;
import java.util.Map;

/**
 * <class-comment/>
 */
public class FileResourceRepositoryTest extends PulseTestCase
{
    private FileResourceRepository repo = null;

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testEmptyRepo() throws Exception
    {
        repo = ResourceFileLoader.load(getInputFile("testEmptyRepo", "xml"));
        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(0, resources.size());
    }

    public void testResource() throws Exception
    {
        repo = ResourceFileLoader.load(getInputFile("testResource", "xml"));
        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(1, resources.size());

        Resource resource = repo.getResource("aResource");
        assertNotNull(resource);
        Map<String, ResourceProperty> props = resource.getProperties();
        assertEquals(1, props.size());
        assertEquals("b", props.get("a").getValue());
    }

    public void testResourceWithVersion() throws Exception
    {
        repo = ResourceFileLoader.load(getInputFile("testResourceWithVersion", "xml"));
        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(1, resources.size());

        Resource resource = repo.getResource("aResource");
        assertNotNull(resource);

        ResourceVersion version = resource.getVersion("aVersion");
        assertNotNull(version);

        Map<String, ResourceProperty> props = version.getProperties();
        assertEquals(2, props.size());
        assertEquals("c", props.get("b").getValue());
        assertEquals("e", props.get("d").getValue());
    }

    public void testMultipleResources() throws Exception
    {
        repo = ResourceFileLoader.load(getInputFile("testMultipleResources", "xml"));

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

    public void testRequirement() throws Exception
    {
        repo = ResourceFileLoader.load(getInputFile("xml"));
        List<SimpleResourceRequirement> reqs = repo.getRequirements();
        assertNotNull(reqs);
        assertEquals(1, reqs.size());

        SimpleResourceRequirement req = reqs.get(0);
        assertEquals("foo", req.getName());
        assertEquals("bar", req.getVersion());
    }
}
