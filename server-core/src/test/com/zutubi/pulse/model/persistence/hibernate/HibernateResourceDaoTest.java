package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.model.Property;
import com.cinnamonbob.core.model.Resource;
import com.cinnamonbob.core.model.ResourceVersion;
import com.cinnamonbob.model.persistence.ResourceDao;

/**
 *
 *
 */
public class HibernateResourceDaoTest extends ServerCorePersistenceTestCase
{
    private ResourceDao resourceDao;

    public void setUp() throws Exception
    {
        super.setUp();
        resourceDao = (ResourceDao) context.getBean("resourceDao");
    }

    public void tearDown() throws Exception
    {
        resourceDao = null;
        super.tearDown();
    }

    public void testLoadSave() throws Exception
    {
        Resource resource = new Resource("resource name");
        Property p1 = new Property("test name", "test value");
        resource.addProperty(p1);

        ResourceVersion version = new ResourceVersion("version value");
        Property p2 = new Property("test name", "test value");
        version.addProperty(p2);
        resource.add(version);

        resourceDao.save(resource);

        commitAndRefreshTransaction();

        Resource otherResource = resourceDao.findById(resource.getId());
        assertEquals(resource.getName(), otherResource.getName());
        assertTrue(otherResource.hasProperty(p1.getName()));
        assertEquals(p1.getValue(), otherResource.getProperty(p1.getName()).getValue());

        assertTrue(otherResource.hasVersion(version.getValue()));
    }
}
