package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.model.CvsRevision;
import com.cinnamonbob.model.persistence.ChangelistDao;

import java.util.Calendar;
import java.util.Date;

/**
 * 
 *
 */
public class HibernateChangelistDaoTest extends MasterPersistenceTestCase
{

    private ChangelistDao changelistDao;

    public void setUp() throws Exception
    {
        super.setUp();
        changelistDao = (ChangelistDao) context.getBean("changelistDao");
    }

    public void tearDown() throws Exception
    {
        changelistDao = null;
        super.tearDown();
    }

    public void testLoadSave() throws Exception
    {
        Date date = Calendar.getInstance().getTime();
        CvsRevision revision = new CvsRevision("bob", "MAIN", "test changelist", date);
        Changelist list = new Changelist(revision);
        Change change = new Change("some/random/file", "23", Change.Action.EDIT);

        list.addChange(change);
        changelistDao.save(list);

        commitAndRefreshTransaction();

        Changelist otherList = changelistDao.findById(list.getId());
        assertPropertyEquals(list, otherList);

        CvsRevision otherRevision = (CvsRevision) otherList.getRevision();
        assertPropertyEquals(revision, otherRevision);

        Change otherChange = otherList.getChanges().get(0);
        assertPropertyEquals(change, otherChange);
    }
}
