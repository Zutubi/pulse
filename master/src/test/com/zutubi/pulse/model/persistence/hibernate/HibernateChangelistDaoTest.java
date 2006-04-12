package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.persistence.ChangelistDao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
        CvsRevision revision = new CvsRevision("pulse", "MAIN", "test changelist", date);
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

    public void testLatestByUser()
    {
        changelistDao.save(createChangelist(1, "login1"));
        changelistDao.save(createChangelist(2, "login2"));
        changelistDao.save(createChangelist(3, "login1"));
        changelistDao.save(createChangelist(4, "login1"));
        changelistDao.save(createChangelist(5, "login2"));

        commitAndRefreshTransaction();

        List<Changelist> changes = changelistDao.findLatestByUser(new User("login1", "Login One"), 10);
        assertEquals(3, changes.size());
        assertEquals("4", changes.get(0).getRevision().getRevisionString());
        assertEquals("3", changes.get(1).getRevision().getRevisionString());
        assertEquals("1", changes.get(2).getRevision().getRevisionString());
    }

    public void testLatestByUserAlias()
    {
        changelistDao.save(createChangelist(1, "login1"));
        changelistDao.save(createChangelist(2, "login2"));
        changelistDao.save(createChangelist(3, "login1"));
        changelistDao.save(createChangelist(4, "alias1"));
        changelistDao.save(createChangelist(5, "alias2"));
        changelistDao.save(createChangelist(6, "alias3"));

        commitAndRefreshTransaction();

        User user = new User("login1", "Login One");
        user.addAlias("alias1");
        user.addAlias("alias3");

        List<Changelist> changes = changelistDao.findLatestByUser(user, 10);
        assertEquals(4, changes.size());
        assertEquals("6", changes.get(0).getRevision().getRevisionString());
        assertEquals("4", changes.get(1).getRevision().getRevisionString());
        assertEquals("3", changes.get(2).getRevision().getRevisionString());
        assertEquals("1", changes.get(3).getRevision().getRevisionString());
    }

    private Changelist createChangelist(long number, String login)
    {
        NumericalRevision revision = new NumericalRevision(number);
        revision.setAuthor(login);
        return new Changelist(revision);
    }
}
