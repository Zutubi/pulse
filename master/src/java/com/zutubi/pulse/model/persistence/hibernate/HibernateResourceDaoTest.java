package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.model.persistence.SlaveDao;
import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.model.Slave;
import junit.framework.Assert;

import java.util.List;

/**
 *
 *
 */
public class HibernateResourceDaoTest extends MasterPersistenceTestCase
{
    private SlaveDao slaveDao;
    private ResourceDao resourceDao;

    public void setUp() throws Exception
    {
        super.setUp();
        slaveDao = (SlaveDao) context.getBean("slaveDao");
        resourceDao = (ResourceDao) context.getBean("resourceDao");
    }

    public void tearDown() throws Exception
    {
        resourceDao = null;
        slaveDao = null;
        super.tearDown();
    }

    public void testLoadSave() throws Exception
    {
        PersistentResource resource = new PersistentResource("resource name");
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

    public void testFindByNullSlave() throws Exception
    {
        Slave s = new Slave();
        slaveDao.save(s);

        PersistentResource p1 = new PersistentResource("foo", s);
        PersistentResource p2 = new PersistentResource("foo", null);
        PersistentResource p3 = new PersistentResource("bar", s);
        PersistentResource p4 = new PersistentResource("bar", null);
        resourceDao.save(p1);
        resourceDao.save(p2);
        resourceDao.save(p3);
        resourceDao.save(p4);

        List<PersistentResource> resources = resourceDao.findAllBySlave(null);
        assertEquals(2, resources.size());
        assertEquals(p2.getId(), resources.get(0).getId());
        assertEquals(p4.getId(), resources.get(1).getId());
    }

    public void testFindBySlave() throws Exception
    {
        Slave s = new Slave();
        slaveDao.save(s);

        PersistentResource p1 = new PersistentResource("foo", s);
        PersistentResource p2 = new PersistentResource("foo", null);
        PersistentResource p3 = new PersistentResource("bar", s);
        PersistentResource p4 = new PersistentResource("bar", null);
        resourceDao.save(p1);
        resourceDao.save(p2);
        resourceDao.save(p3);
        resourceDao.save(p4);

        List<PersistentResource> resources = resourceDao.findAllBySlave(s);
        assertEquals(2, resources.size());
        assertEquals(p1.getId(), resources.get(0).getId());
        assertEquals(p3.getId(), resources.get(1).getId());
    }

    public void testFindByNullSlaveAndName() throws Exception
    {
        Slave s = new Slave();
        slaveDao.save(s);

        PersistentResource p1 = new PersistentResource("foo", s);
        PersistentResource p2 = new PersistentResource("foo", null);
        PersistentResource p3 = new PersistentResource("bar", s);
        PersistentResource p4 = new PersistentResource("bar", null);
        resourceDao.save(p1);
        resourceDao.save(p2);
        resourceDao.save(p3);
        resourceDao.save(p4);

        PersistentResource resource = resourceDao.findBySlaveAndName(null, "foo");
        assertEquals(p2.getId(), resource.getId());
    }

    public void testFindBySlaveAndName() throws Exception
    {
        Slave s = new Slave();
        slaveDao.save(s);

        PersistentResource p1 = new PersistentResource("foo", s);
        PersistentResource p2 = new PersistentResource("foo", null);
        PersistentResource p3 = new PersistentResource("bar", s);
        PersistentResource p4 = new PersistentResource("bar", null);
        resourceDao.save(p1);
        resourceDao.save(p2);
        resourceDao.save(p3);
        resourceDao.save(p4);

        PersistentResource resource = resourceDao.findBySlaveAndName(s, "foo");
        assertEquals(p1.getId(), resource.getId());
    }
}
