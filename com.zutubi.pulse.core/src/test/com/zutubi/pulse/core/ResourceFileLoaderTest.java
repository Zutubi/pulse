package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.config.ResourceVersionConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ResourceFileLoaderTest extends PulseTestCase
{
    private static final String EXTENSION_XML = "xml";

    private ResourceFileLoader loader;

    protected void setUp() throws Exception
    {
        super.setUp();

        TypeRegistry registry = new TypeRegistry();
        registry.register(ResourcesConfiguration.class);
        loader = new ResourceFileLoader();
        loader.setTypeRegistry(registry);
        loader.setObjectFactory(new DefaultObjectFactory());
        loader.init();
    }

    public void testEmptyRepo() throws Exception
    {
        InMemoryResourceRepository repo = load();
        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(0, resources.size());
    }

    public void testResource() throws Exception
    {
        InMemoryResourceRepository repo = load();
        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(1, resources.size());

        ResourceConfiguration resource = repo.getResource("aResource");
        assertNotNull(resource);
        Map<String, ResourcePropertyConfiguration> props = resource.getProperties();
        assertEquals(1, props.size());
        assertEquals("b", props.get("a").getValue());
    }

    public void testResourceWithVersion() throws Exception
    {
        InMemoryResourceRepository repo = load();
        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(1, resources.size());

        ResourceConfiguration resource = repo.getResource("aResource");
        assertNotNull(resource);

        ResourceVersionConfiguration version = resource.getVersion("aVersion");
        assertNotNull(version);

        Map<String, ResourcePropertyConfiguration> props = version.getProperties();
        assertEquals(2, props.size());
        assertEquals("c", props.get("b").getValue());
        assertEquals("e", props.get("d").getValue());
    }

    public void testMultipleResources() throws Exception
    {
        InMemoryResourceRepository repo = load();

        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(2, resources.size());

        ResourceConfiguration resource = repo.getResource("aResource");
        assertNotNull(resource);

        resource = repo.getResource("bResource");
        assertNotNull(resource);
        assertNotNull(resource.getVersion("aVersion"));
        assertNotNull(resource.getVersion("bVersion"));
    }

    private InMemoryResourceRepository load() throws PulseException, IOException
    {
        return loader.load(getInputFile(EXTENSION_XML));
    }
}
