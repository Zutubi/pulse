package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceVersionDao;
import junit.framework.Assert;

/**
 *
 *
 */
public class HibernateResourceVersionDaoTest extends MasterPersistenceTestCase
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
        ResourceProperty p1 = new ResourceProperty("test name", "test value", true, true);
        ResourceProperty p2 = new ResourceProperty("another name", "and value", true, true);
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
