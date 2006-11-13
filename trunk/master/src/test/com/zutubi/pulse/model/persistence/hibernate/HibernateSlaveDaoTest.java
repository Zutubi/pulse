package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.persistence.SlaveDao;

import java.util.List;

/**
 * @noinspection FieldCanBeLocal
 */
public class HibernateSlaveDaoTest extends MasterPersistenceTestCase
{
    private SlaveDao slaveDao;

    public void setUp() throws Exception
    {
        super.setUp();
        slaveDao = (SlaveDao) context.getBean("slaveDao");
    }

    public void tearDown() throws Exception
    {
        slaveDao = null;
        super.tearDown();
    }

    public void testSaveAndLoad()
    {
        Slave slave = new Slave("test name", "test host");
        slaveDao.save(slave);
        commitAndRefreshTransaction();

        Slave anotherSlave = slaveDao.findById(slave.getId());

        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(slave == anotherSlave);
        assertPropertyEquals(slave, anotherSlave);
    }

    public void testFindAll()
    {
        List slaves = slaveDao.findAll();
        assertNotNull(slaves);
        assertEquals(0, slaves.size());

        Slave slave = new Slave();
        slaveDao.save(slave);
        commitAndRefreshTransaction();

        slaves = slaveDao.findAll();
        assertNotNull(slaves);
        assertEquals(1, slaves.size());

        slave = new Slave();
        slaveDao.save(slave);
        commitAndRefreshTransaction();

        slaves = slaveDao.findAll();
        assertNotNull(slaves);
        assertEquals(2, slaves.size());
    }

}
