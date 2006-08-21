package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.persistence.UserDao;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.GroupDao;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.EmailContactPoint;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.Group;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

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
}
