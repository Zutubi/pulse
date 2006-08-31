package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.persistence.GroupDao;
import com.zutubi.pulse.model.persistence.UserDao;

import java.util.List;

/**
 * @noinspection FieldCanBeLocal
 */
public class HibernateGroupDaoTest extends MasterPersistenceTestCase
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
        User user = new User("login", "name");
        userDao.save(user);

        Group group = new Group("gname");
        group.addUser(user);
        group.addAdditionalAuthority("ROLE_ADMINISTRATOR");
        groupDao.save(group);

        commitAndRefreshTransaction();

        Group anotherGroup = groupDao.findById(group.getId());

        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(group == anotherGroup);
        assertPropertyEquals(group, anotherGroup);
    }

    public void testFindByName()
    {
        groupDao.save(new Group("name1"));
        groupDao.save(new Group("name2"));

        commitAndRefreshTransaction();

        Group g = groupDao.findByName("name1");
        assertEquals("name1", g.getName());
        assertNull(groupDao.findByName("nosuchname"));
    }

    public void testFindByMember()
    {
        User u1 = new User("login", "name");
        User u2 = new User("login2", "name2");
        userDao.save(u1);
        userDao.save(u2);

        Group g1 = new Group("gname");
        g1.addUser(u1);
        g1.addUser(u2);
        groupDao.save(g1);

        Group g2 = new Group("gname2");
        g2.addUser(u1);
        groupDao.save(g2);

        commitAndRefreshTransaction();

        List<Group> groups = groupDao.findByMember(u1);
        assertEquals(2, groups.size());

        groups = groupDao.findByMember(u2);
        assertEquals(1, groups.size());
        assertEquals(g1, groups.get(0));
    }
}
