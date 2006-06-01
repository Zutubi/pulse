package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.Property;
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
        Property p1 = new Property("test name", "test value");
        Property p2 = new Property("another name", "and value");
        version.addProperty(p1);
        version.addProperty(p2);
        resourceVersionDao.save(version);

        commitAndRefreshTransaction();

        ResourceVersion otherVersion = resourceVersionDao.findById(version.getId());
        Assert.assertEquals(version.getValue(), otherVersion.getValue());
        Assert.assertTrue(otherVersion.hasProperty(p1.getName()));
        Assert.assertTrue(otherVersion.hasProperty(p2.getName()));
        Assert.assertEquals(p1.getValue(), otherVersion.getProperty(p1.getName()).getValue());
        Assert.assertEquals(p2.getValue(), otherVersion.getProperty(p2.getName()).getValue());
    }
}
