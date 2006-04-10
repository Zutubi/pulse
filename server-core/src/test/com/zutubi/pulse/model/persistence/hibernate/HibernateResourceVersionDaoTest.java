package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.model.Property;
import com.cinnamonbob.core.model.ResourceVersion;
import com.cinnamonbob.model.persistence.ResourceVersionDao;

/**
 *
 *
 */
public class HibernateResourceVersionDaoTest extends ServerCorePersistenceTestCase
{
    private ResourceVersionDao resourceVersionDao;

    public void setUp() throws Exception
    {
        super.setUp();
        resourceVersionDao = (ResourceVersionDao) context.getBean("resourceVersionDao");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testLoadSave() throws Exception
    {
        ResourceVersion version = new ResourceVersion("version value");
        Property p1 = new Property("test name", "test value");
        Property p2 = new Property("another name", "and value");
        version.addProperty(p1);
        version.addProperty(p2);
        resourceVersionDao.save(version);

        commitAndRefreshTransaction();

        ResourceVersion otherVersion = resourceVersionDao.findById(version.getId());
        assertEquals(version.getValue(), otherVersion.getValue());
        assertTrue(otherVersion.hasProperty(p1.getName()));
        assertTrue(otherVersion.hasProperty(p2.getName()));
        assertEquals(p1.getValue(), otherVersion.getProperty(p1.getName()).getValue());
        assertEquals(p2.getValue(), otherVersion.getProperty(p2.getName()).getValue());
    }
}
