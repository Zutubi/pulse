package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.CvsRevision;
import com.cinnamonbob.model.SimpleChange;
import com.cinnamonbob.model.SimpleChangelist;
import com.cinnamonbob.model.persistence.ChangelistDao;
import com.cinnamonbob.scm.Change;

import java.util.Calendar;
import java.util.Date;

/**
 * 
 *
 */
public class HibernateChangelistDaoTest extends PersistenceTestCase
{

    private ChangelistDao changelistDao;

    public void setUp() throws Exception
    {
        super.setUp();
        changelistDao = (ChangelistDao) context.getBean("changelistDao");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testLoadSave()
    {
        try {
            Date date = Calendar.getInstance().getTime();
            CvsRevision revision = new CvsRevision("bob", "MAIN", "test changelist", date);
            SimpleChangelist list = new SimpleChangelist(revision, date, "bob", "wow, this is teh r0x0rz");
            SimpleChange change = new SimpleChange("some/random/file", "23", Change.Action.EDIT);

            list.addChange(change);
            changelistDao.save(list);

            commitAndRefreshTransaction();

            SimpleChangelist otherList = changelistDao.findById(list.getId());
            assertEquals(list.getUser(), otherList.getUser());
            assertEquals(list.getDate(), otherList.getDate());
            assertEquals(list.getComment(), otherList.getComment());
            assertEquals(list.getChanges().size(), otherList.getChanges().size());

            CvsRevision otherRevision = (CvsRevision)otherList.getRevision();
            assertEquals(revision.getAuthor(), otherRevision.getAuthor());
            assertEquals(revision.getBranch(), otherRevision.getBranch());
            assertEquals(revision.getComment(), otherRevision.getComment());
            assertEquals(revision.getDate(), otherRevision.getDate());

            SimpleChange otherChange = (SimpleChange)otherList.getChanges().get(0);
            assertEquals(change.getAction(), otherChange.getAction());
            assertEquals(change.getFilename(), otherChange.getFilename());
            assertEquals(change.getRevision(), otherChange.getRevision());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
