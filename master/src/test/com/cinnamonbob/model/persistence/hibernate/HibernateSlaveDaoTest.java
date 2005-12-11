package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.Slave;
import com.cinnamonbob.model.persistence.SlaveDao;

import java.util.List;

/**
 * @noinspection FieldCanBeLocal
 */
public class HibernateSlaveDaoTest extends PersistenceTestCase
{
    private SlaveDao slaveDao;

    public void setUp() throws Exception
    {
        super.setUp();
        slaveDao = (SlaveDao) context.getBean("slaveDao");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSaveAndLoad()
    {
        Slave slave = new Slave("test name", "test host");
        slave.lastPing(11, false);
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
        Slave slave = new Slave();
        slaveDao.save(slave);
        commitAndRefreshTransaction();

        List slaves = slaveDao.findAll();
        assertNotNull(slaves);
        assertEquals(1, slaves.size());
    }

}
