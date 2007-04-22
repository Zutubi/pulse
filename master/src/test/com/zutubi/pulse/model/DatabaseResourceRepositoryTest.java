package com.zutubi.pulse.model;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.model.persistence.hibernate.MasterPersistenceTestCase;

/**
 * <class comment/>
 */
public class DatabaseResourceRepositoryTest extends MasterPersistenceTestCase
{
    private DatabaseResourceRepository repository;
    private ResourceDao resourceDao;

    protected void setUp() throws Exception
    {
        super.setUp();

        resourceDao = (ResourceDao) context.getBean("resourceDao");
        repository = new DatabaseResourceRepository(resourceDao);
    }

    protected void tearDown() throws Exception
    {
        repository = null;
        resourceDao = null;

        super.tearDown();
    }

    public void testAddResource()
    {
        Resource resource = new Resource("a");
        resource.addProperty(new ResourceProperty("p", "v", false, false, false));

        repository.addResource(resource);
        commitAndRefreshTransaction();

        assertTrue(repository.hasResource("a"));

        Resource persistentResource = repository.getResource("a");
        assertTrue(persistentResource.hasProperty("p"));
        
        ResourceProperty persistentProperty = persistentResource.getProperty("p");
        assertEquals("v", persistentProperty.getValue());
        assertFalse(persistentProperty.getAddToEnvironment());
        assertFalse(persistentProperty.getAddToPath());
    }

    public void testAddResourceOverwrite()
    {
        Resource original = new Resource("a");
        original.addProperty(new ResourceProperty("p", "v", false, false, false));

        repository.addResource(original);
        commitAndRefreshTransaction();

        Resource update = new Resource("a");
        update.addProperty(new ResourceProperty("p", "value", true, true, true));

        // check that overwrite works.
        repository.addResource(update, true);
        commitAndRefreshTransaction();

        Resource persistentResource = repository.getResource("a");
        assertTrue(persistentResource.hasProperty("p"));

        ResourceProperty persistentProperty = persistentResource.getProperty("p");
        assertEquals("value", persistentProperty.getValue());
        assertTrue(persistentProperty.getAddToEnvironment());
        assertTrue(persistentProperty.getAddToPath());

        // check that overwrite false does not overwrite.
        repository.addResource(original, false);
        commitAndRefreshTransaction();

        persistentResource = repository.getResource("a");
        assertTrue(persistentResource.hasProperty("p"));

        persistentProperty = persistentResource.getProperty("p");
        assertEquals("value", persistentProperty.getValue());
        assertTrue(persistentProperty.getAddToEnvironment());
        assertTrue(persistentProperty.getAddToPath());
    }

    public void testAddResourceVersion() throws FileLoadException
    {
        Resource resource = new Resource("a");
        ResourceVersion version = new ResourceVersion("b");
        version.addProperty(new ResourceProperty("p", "v", false, false, false));
        resource.add(version);
        
        repository.addResource(resource);
        commitAndRefreshTransaction();

        assertTrue(repository.hasResource("a"));

        Resource persistentResource = repository.getResource("a");
        assertTrue(persistentResource.hasVersion("b"));

        ResourceVersion persistentVersion = persistentResource.getVersion("b");
        assertTrue(persistentVersion.hasProperty("p"));

        ResourceProperty persistentProperty = persistentVersion.getProperty("p");
        assertEquals("v", persistentProperty.getValue());
        assertFalse(persistentProperty.getAddToEnvironment());
        assertFalse(persistentProperty.getAddToPath());
    }

    public void testAddResourceVersionOverwrite() throws FileLoadException
    {
        Resource resource = new Resource("a");
        ResourceVersion version = new ResourceVersion("b");
        version.addProperty(new ResourceProperty("p", "v", false, false, false));
        resource.add(version);

        repository.addResource(resource);
        commitAndRefreshTransaction();

        Resource updatedResource = new Resource("a");
        ResourceVersion updatedVersion = new ResourceVersion("b");
        updatedVersion.addProperty(new ResourceProperty("p", "value", true, true, true));
        updatedResource.add(updatedVersion);

        repository.addResource(updatedResource, true);
        commitAndRefreshTransaction();

        assertTrue(repository.hasResource("a"));

        Resource persistentResource = repository.getResource("a");
        assertTrue(persistentResource.hasVersion("b"));

        ResourceVersion persistentVersion = persistentResource.getVersion("b");
        assertTrue(persistentVersion.hasProperty("p"));

        ResourceProperty persistentProperty = persistentVersion.getProperty("p");
        assertEquals("value", persistentProperty.getValue());
        assertTrue(persistentProperty.getAddToEnvironment());
        assertTrue(persistentProperty.getAddToPath());

        repository.addResource(resource, false);
        commitAndRefreshTransaction();

        persistentResource = repository.getResource("a");
        assertTrue(persistentResource.hasVersion("b"));

        persistentVersion = persistentResource.getVersion("b");
        assertTrue(persistentVersion.hasProperty("p"));

        persistentProperty = persistentVersion.getProperty("p");
        assertEquals("value", persistentProperty.getValue());
        assertTrue(persistentProperty.getAddToEnvironment());
        assertTrue(persistentProperty.getAddToPath());
    }
}
