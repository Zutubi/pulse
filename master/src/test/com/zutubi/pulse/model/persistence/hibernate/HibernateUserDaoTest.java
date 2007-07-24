package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.persistence.GroupDao;
import com.zutubi.pulse.model.persistence.UserDao;

import java.util.List;

/**
 * @noinspection FieldCanBeLocal
 */
public class HibernateUserDaoTest extends MasterPersistenceTestCase
{
    private UserDao userDao;
    private GroupDao groupDao;

    public void setUp() throws Exception
    {
        super.setUp();
        userDao = (UserDao) context.getBean("userDao");
        groupDao = (GroupDao) context.getBean("groupDao");
    }

    public void tearDown() throws Exception
    {
        userDao = null;
        groupDao = null;
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
