package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.model.TestCaseIndex;
import com.zutubi.pulse.master.model.persistence.TestCaseIndexDao;

import java.util.List;

/**
 *
 *
 */
public class HibernateTestCaseIndexDaoTest extends MasterPersistenceTestCase
{
    private TestCaseIndexDao testCaseIndexDao;

    public void setUp() throws Exception
    {
        super.setUp();
        testCaseIndexDao = (TestCaseIndexDao) context.getBean("testCaseIndexDao");
    }

    public void tearDown() throws Exception
    {
        testCaseIndexDao = null;
        super.tearDown();
    }

    public void testLoadSave() throws Exception
    {
        TestCaseIndex index = new TestCaseIndex(101, 123, "some/test/case");
        testCaseIndexDao.save(index);
        commitAndRefreshTransaction();

        TestCaseIndex otherIndex = testCaseIndexDao.findById(index.getId());
        assertPropertyEquals(index, otherIndex);
    }

    public void testFindBySuite() throws Exception
    {
        TestCaseIndex i1 = new TestCaseIndex(1, 101, "suite1/case1");
        TestCaseIndex i2 = new TestCaseIndex(1, 101, "suite2/case1");
        TestCaseIndex i3 = new TestCaseIndex(1, 101, "suite1/case2");
        TestCaseIndex i4 = new TestCaseIndex(2, 201, "suite1/case1");

        testCaseIndexDao.save(i1);
        testCaseIndexDao.save(i2);
        testCaseIndexDao.save(i3);
        testCaseIndexDao.save(i4);

        commitAndRefreshTransaction();

        List<TestCaseIndex> found = testCaseIndexDao.findBySuite(101, "suite1");
        assertEquals(2, found.size());
        assertPropertyEquals(i1, found.get(0));
        assertPropertyEquals(i3, found.get(1));
    }

    public void testFindByStage() throws Exception
    {
        TestCaseIndex i1 = new TestCaseIndex(1, 101, "suite1/case1");
        TestCaseIndex i2 = new TestCaseIndex(1, 101, "suite2/case1");
        TestCaseIndex i3 = new TestCaseIndex(1, 101, "suite1/case2");
        TestCaseIndex i4 = new TestCaseIndex(2, 201, "suite1/case1");

        testCaseIndexDao.save(i1);
        testCaseIndexDao.save(i2);
        testCaseIndexDao.save(i3);
        testCaseIndexDao.save(i4);

        commitAndRefreshTransaction();

        List<TestCaseIndex> found = testCaseIndexDao.findByStage(101);
        assertEquals(3, found.size());
        assertPropertyEquals(i1, found.get(0));
        assertPropertyEquals(i2, found.get(1));
        assertPropertyEquals(i3, found.get(2));
    }
}
