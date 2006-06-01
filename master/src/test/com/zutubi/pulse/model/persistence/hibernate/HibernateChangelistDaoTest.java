package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.model.Project;
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
        Changelist list = new Changelist("scm", revision);
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

    public void testLatestForProject()
    {
        changelistDao.save(createChangelist(1, 1, "login1"));
        changelistDao.save(createChangelist(1, 2, "login2"));
        changelistDao.save(createChangelist(2, 3, "login1"));
        changelistDao.save(createChangelist(1, 4, "login1"));
        changelistDao.save(createChangelist(2, 5, "login2"));

        commitAndRefreshTransaction();
        Project p = new Project();
        p.setId(1);

        List<Changelist> changes = changelistDao.findLatestByProject(p, 10);
        assertEquals(3, changes.size());
        assertEquals("4", changes.get(0).getRevision().getRevisionString());
        assertEquals("2", changes.get(1).getRevision().getRevisionString());
        assertEquals("1", changes.get(2).getRevision().getRevisionString());
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

    public void testLookupByRevision()
    {
        changelistDao.save(createChangelist(12, "jason"));

        commitAndRefreshTransaction();

        Changelist changelist = changelistDao.findByRevision("scm", new NumericalRevision(12));
        assertNotNull(changelist);
        assertEquals("jason", changelist.getRevision().getAuthor());
    }

    public void testLookupByCvsRevision()
    {
        Revision r = new CvsRevision("joe", "MAIN", "i made this", new Date(1234));
        Changelist list = new Changelist("scm", r);

        changelistDao.save(list);

        commitAndRefreshTransaction();

        Changelist otherList = changelistDao.findByRevision("scm", r);
        assertNotNull(otherList);
        assertPropertyEquals(list, otherList);
    }

    private Changelist createChangelist(long project, long number, String login)
    {
        NumericalRevision revision = new NumericalRevision(number);
        revision.setAuthor(login);
        // generate time stamps to provide the same ordering as the revision numbers.
        revision.setDate(new Date(System.currentTimeMillis() + number));
        Changelist changelist = new Changelist("scm", revision);

        if(project != 0)
        {
            changelist.addProjectId(project);
        }

        return changelist;
    }

    private Changelist createChangelist(long number, String login)
    {
        return createChangelist(0, number, login);
    }
}
