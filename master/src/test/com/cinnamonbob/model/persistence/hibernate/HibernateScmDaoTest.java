package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.persistence.ScmDao;
import com.cinnamonbob.model.Svn;

/**
 * 
 *
 */
public class HibernateScmDaoTest extends PersistenceTestCase
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
}
