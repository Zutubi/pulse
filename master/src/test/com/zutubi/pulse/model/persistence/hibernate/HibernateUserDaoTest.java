package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.EmailContactPoint;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.UserDao;
import com.zutubi.pulse.model.persistence.ProjectDao;

import java.util.List;
import java.util.LinkedList;

/**
 * @noinspection FieldCanBeLocal
 */
public class HibernateUserDaoTest extends MasterPersistenceTestCase
{
    private UserDao userDao;
    private ProjectDao projectDao;

    public void setUp() throws Exception
    {
        super.setUp();
        userDao = (UserDao) context.getBean("userDao");
        projectDao = (ProjectDao) context.getBean("projectDao");
    }

    public void tearDown() throws Exception
    {
        userDao = null;
        super.tearDown();
    }

    public void testSaveAndLoad()
    {
        User user = new User();
        user.setLogin("login");
        user.setName("name");
        user.setPassword("some password");
        user.setEnabled(true);
        userDao.save(user);
        commitAndRefreshTransaction();

        User anotherUser = userDao.findById(user.getId());

        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(user == anotherUser);
        assertPropertyEquals(user, anotherUser);
    }

    public void testFindAll()
    {
        User user = new User();
        userDao.save(user);
        commitAndRefreshTransaction();

        List users = userDao.findAll();
        assertNotNull(users);
        assertEquals(1, users.size());
    }

    public void testUserContactPoints()
    {
        User user = new User();
        userDao.save(user);
        EmailContactPoint email = new EmailContactPoint();
        email.setName("home");
        email.setEmail("daniel@home.com");
        user.add(email);
        userDao.save(user);
        commitAndRefreshTransaction();

        user = userDao.findById(user.getId());
        assertEquals(1, user.getContactPoints().size());
        EmailContactPoint otherEmail = (EmailContactPoint) user.getContactPoints().get(0);
        assertPropertyEquals(email, otherEmail);
    }

    public void testAliases()
    {
        User user = new User();
        user.addAlias("help me");
        user.addAlias("rhonda");
        userDao.save(user);
        commitAndRefreshTransaction();

        user = userDao.findById(user.getId());
        assertEquals(2, user.getAliases().size());
        assertTrue(user.hasAlias("help me"));
        assertTrue(user.hasAlias("rhonda"));
    }

    public void testProjects()
    {
        List<Project> projects = new LinkedList<Project>();
        Project p1 = new Project("1", "project 1");
        Project p2 = new Project("2", "project 2");
        projectDao.save(p1);
        projectDao.save(p2);
        projects.add(p1);
        projects.add(p2);

        User user = new User();
        user.setShowAllProjects(true);
        user.setProjects(projects);
        userDao.save(user);
        commitAndRefreshTransaction();

        user = userDao.findById(user.getId());
        assertTrue(user.getShowAllProjects());

        List<Project> otherProjects = userDao.getProjects(user);
        assertEquals(2, otherProjects.size());
        assertEquals("1", otherProjects.get(0).getName());
        assertEquals("2", otherProjects.get(1).getName());
    }

    public void testPropertyPersistence()
    {
        User user = new User();
        userDao.save(user);
        commitAndRefreshTransaction();
        user = userDao.findById(user.getId());

        assertFalse(user.hasProperty("a"));
        assertNull(user.getProperty("a"));
        user.setProperty("a", "b");
        assertTrue(user.hasProperty("a"));
        assertEquals("b", user.getProperty("a"));
        commitAndRefreshTransaction();

        user = userDao.findById(user.getId());
        assertTrue(user.hasProperty("a"));
        assertEquals("b", user.getProperty("a"));
    }
/*
    public void testGrantedAuthorities()
    {
        User user = new User();
        userDao.save(user);

        UserAuthority authority = new UserAuthority();
        authority.setAuthority("SUPER_USER");
        user.add(authority);
        userDao.save(user);
        commitAndRefreshTransaction();

        user = userDao.findById(user.getId());
        assertEquals(1, user.getUserAuthorities().size());
        UserAuthority otherAuthority = user.getUserAuthorities().get(0);
        assertPropertyEquals(authority, otherAuthority);
    }
*/
}
