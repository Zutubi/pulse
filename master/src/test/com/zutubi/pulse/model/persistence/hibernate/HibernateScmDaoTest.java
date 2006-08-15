package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.P4;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.model.Svn;
import com.zutubi.pulse.model.persistence.ScmDao;

import java.util.List;

/**
 * 
 *
 */
public class HibernateScmDaoTest extends MasterPersistenceTestCase
{

    private ScmDao scmDao;

    public void setUp() throws Exception
    {
        super.setUp();
        scmDao = (ScmDao) context.getBean("scmDao");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testLoadSave()
    {
        Svn svn = new Svn();
        svn.setUrl("http://blah.com/svn/root");
        scmDao.save(svn);

        commitAndRefreshTransaction();

        Svn otherSvn = (Svn) scmDao.findById(svn.getId());
        assertPropertyEquals(svn, otherSvn);
    }

    public void testFindAllActive()
    {
        Scm scm = new Svn();
        scmDao.save(scm);

        commitAndRefreshTransaction();

        assertActiveScms(0);

        scm = scmDao.findById(1);

        scm.setMonitor(true);
        scmDao.save(scm);

        commitAndRefreshTransaction();

        assertActiveScms(1);

        Scm other = new P4();
        scmDao.save(other);

        commitAndRefreshTransaction();

        assertActiveScms(1);

        other.setMonitor(true);
        scmDao.save(other);

        commitAndRefreshTransaction();

        assertActiveScms(2);
    }

    private void assertActiveScms(int activeCount)
    {
        List<Scm> activeScms = scmDao.findAllActive();
        assertNotNull(activeScms);
        assertEquals(activeCount, activeScms.size());
    }
}
