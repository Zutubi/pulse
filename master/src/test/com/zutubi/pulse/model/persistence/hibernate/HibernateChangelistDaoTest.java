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

    public void testLatestForProjects()
    {
        changelistDao.save(createChangelist(1, 1, "login1"));
        changelistDao.save(createChangelist(1, 2, "login2"));
        changelistDao.save(createChangelist(2, 3, "login1"));
        changelistDao.save(createChangelist(1, 4, "login1"));
        changelistDao.save(createChangelist(2, 5, "login2"));
        changelistDao.save(createChangelist(3, 6, "login2"));
        changelistDao.save(createChangelist(3, 7, "login2"));
        changelistDao.save(createChangelist(2, 8, "login2"));

        commitAndRefreshTransaction();
        Project p1 = new Project();
        p1.setId(1);
        Project p3 = new Project();
        p3.setId(3);

        List<Changelist> changes = changelistDao.findLatestByProjects(new Project[] {p1, p3}, 4);
        assertEquals(4, changes.size());
        assertEquals("7", changes.get(0).getRevision().getRevisionString());
        assertEquals("6", changes.get(1).getRevision().getRevisionString());
        assertEquals("4", changes.get(2).getRevision().getRevisionString());
        assertEquals("2", changes.get(3).getRevision().getRevisionString());
    }

    public void testLatestForProjectsOverlapping()
    {
        Changelist change = createChangelist(1, 1, "login1");
        change.addProjectId(3);
        changelistDao.save(change);
        commitAndRefreshTransaction();

        Project p1 = new Project();
        p1.setId(1);
        Project p3 = new Project();
        p3.setId(3);

        List<Changelist> changes = changelistDao.findLatestByProjects(new Project[] {p1, p3}, 10);
        assertEquals(1, changes.size());
        assertEquals("1", changes.get(0).getRevision().getRevisionString());
    }

    public void testLatestForProjectsOverlappingStillGetMax()
    {
        Changelist change = createChangelist(1, 1, "login1");
        change.addProjectId(3);
        changelistDao.save(change);
        changelistDao.save(createChangelist(1, 2, "login1"));
        commitAndRefreshTransaction();

        Project p1 = new Project();
        p1.setId(1);
        Project p3 = new Project();
        p3.setId(3);

        List<Changelist> changes = changelistDao.findLatestByProjects(new Project[] {p1, p3}, 10);
        assertEquals(2, changes.size());
        assertEquals("2", changes.get(0).getRevision().getRevisionString());
        assertEquals("1", changes.get(1).getRevision().getRevisionString());
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

    public void testFindByResult()
    {
        Changelist list = new Changelist("uid", new NumericalRevision(1));
        list.addResultId(12);
        changelistDao.save(list);
        commitAndRefreshTransaction();

        List<Changelist> found = changelistDao.findByResult(1);
        assertEquals(0, found.size());
        found = changelistDao.findByResult(12);
        assertEquals(1, found.size());
        assertEquals(list, found.get(0));
    }

    public void testFindByResultDistinct()
    {
        NumericalRevision r1 = new NumericalRevision(1);
        r1.setDate(new Date(System.currentTimeMillis() - 1000));
        Changelist l1 = new Changelist("uid", r1);
        l1.addResultId(12);
        l1.addResultId(13);
        l1.addResultId(14);
        l1.addChange(new Change("file1", "rev", Change.Action.ADD));
        l1.addChange(new Change("file2", "rev", Change.Action.ADD));
        l1.addChange(new Change("file3", "rev", Change.Action.ADD));
        changelistDao.save(l1);

        NumericalRevision r2 = new NumericalRevision(100);
        r2.setDate(new Date(System.currentTimeMillis()));
        Changelist l2 = new Changelist("uid", r2);
        l2.addResultId(13);
        l2.addResultId(14);
        l2.addResultId(15);
        l2.addChange(new Change("file1", "rev", Change.Action.ADD));
        l2.addChange(new Change("file2", "rev", Change.Action.ADD));
        l2.addChange(new Change("file3", "rev", Change.Action.ADD));
        changelistDao.save(l2);
        commitAndRefreshTransaction();

        List<Changelist> found = changelistDao.findByResult(13);
        assertEquals(2, found.size());
        assertEquals(l2, found.get(0));
        assertEquals(l1, found.get(1));
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
