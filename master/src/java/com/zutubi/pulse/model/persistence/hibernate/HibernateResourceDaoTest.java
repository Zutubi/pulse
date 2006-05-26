/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceDao;
import junit.framework.Assert;

/**
 *
 *
 */
public class HibernateResourceDaoTest extends MasterPersistenceTestCase
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
        Assert.assertEquals(resource.getName(), otherResource.getName());
        Assert.assertTrue(otherResource.hasProperty(p1.getName()));
        Assert.assertEquals(p1.getValue(), otherResource.getProperty(p1.getName()).getValue());

        Assert.assertTrue(otherResource.hasVersion(version.getValue()));
    }
}
