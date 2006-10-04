package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.EmailContactPoint;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.UserDao;
import com.zutubi.pulse.model.persistence.GroupDao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @noinspection FieldCanBeLocal
 */
public class HibernateUserDaoTest extends MasterPersistenceTestCase
{
    private UserDao userDao;
    private GroupDao groupDao;
    private ProjectDao projectDao;

    public void setUp() throws Exception
    {
        super.setUp();
        userDao = (UserDao) context.getBean("userDao");
        groupDao = (GroupDao) context.getBean("groupDao");
        projectDao = (ProjectDao) context.getBean("projectDao");
    }

    public void tearDown() throws Exception
    {
        userDao = null;
        groupDao = null;
        projectDao = null;
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
        User user = new User("login", "name");
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
        Set<Project> projects = new HashSet<Project>();
        Project p1 = new Project("1", "project 1");
        Project p2 = new Project("2", "project 2");
        projectDao.save(p1);
        projectDao.save(p2);
        projects.add(p1);
        projects.add(p2);

        User user = new User();
        user.setHiddenProjects(projects);
        userDao.save(user);
        commitAndRefreshTransaction();

        user = userDao.findById(user.getId());

        Set<Project> otherProjects = userDao.getHiddenProjects(user);
        assertEquals(2, otherProjects.size());
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

    public void testFindByNotInGroup()
    {
        User u1 = new User("l1", "n1");
        User u2 = new User("l2", "n2");

        userDao.save(u1);
        userDao.save(u2);

        Group g1 = new Group("g1");
        Group g2 = new Group("g2");
        Group g3 = new Group("g2");

        g2.addUser(u1);
        g3.addUser(u1);
        g3.addUser(u2);

        groupDao.save(g1);
        groupDao.save(g2);
        groupDao.save(g3);

        commitAndRefreshTransaction();

        List<User> users = userDao.findByNotInGroup(g1);
        assertEquals(2, users.size());
        assertEquals(u1, users.get(0));
        assertEquals(u2, users.get(1));

        users = userDao.findByNotInGroup(g2);
        assertEquals(1, users.size());
        assertEquals(u2, users.get(0));

        users = userDao.findByNotInGroup(g3);
        assertEquals(0, users.size());
    }

    public void testFindByHiddenProject()
    {
        Set<Project> projects = new HashSet<Project>();
        Project p1 = new Project("1", "project 1");
        Project p2 = new Project("2", "project 2");
        projectDao.save(p1);
        projectDao.save(p2);
        projects.add(p1);

        User user = new User();
        user.setHiddenProjects(projects);
        userDao.save(user);
        commitAndRefreshTransaction();

        List<User> users = userDao.findByHiddenProject(p1);
        assertEquals(1, users.size());
        users = userDao.findByHiddenProject(p2);
        assertEquals(0, users.size());
    }

    public void testFindVisibleProjectsByUser()
    {
        Project p1 = new Project("1", "project 1");
        Project p2 = new Project("2", "project 2");
        projectDao.save(p1);
        projectDao.save(p2);

        // A) Mark project 1 as hidden. Project 2 should be visible.

        Set<Project> projects = new HashSet<Project>();
        projects.add(p1);

        User user = new User();
        user.setHiddenProjects(projects);
        userDao.save(user);
        commitAndRefreshTransaction();

        List<Project> visibleProjects = userDao.findVisibleProjectsByUser(user);
        assertEquals(1, visibleProjects.size());
        assertEquals(p2, visibleProjects.get(0));

        // B) Mark project 2 as hidden, no projects should be visible.

        projects.add(projectDao.findById(p2.getId()));
        user.setHiddenProjects(projects);
        userDao.save(user);
        commitAndRefreshTransaction();

        // verify that no projects are visible.
        visibleProjects = userDao.findVisibleProjectsByUser(user);
        assertEquals(0, visibleProjects.size());

        // C) Mark no projects as hidden. All projects should be visible.

        projects.clear();
        user.setHiddenProjects(projects);
        userDao.save(user);
        commitAndRefreshTransaction();

        // verify that all projects are visible.
        visibleProjects = userDao.findVisibleProjectsByUser(user);
        assertEquals(2, visibleProjects.size());
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
