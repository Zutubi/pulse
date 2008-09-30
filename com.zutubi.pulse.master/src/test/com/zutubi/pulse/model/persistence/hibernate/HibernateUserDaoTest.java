package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.persistence.UserDao;

import java.util.List;

/**
 * @noinspection FieldCanBeLocal
 */
public class HibernateUserDaoTest extends MasterPersistenceTestCase
{
    private UserDao userDao;

    public void setUp() throws Exception
    {
        super.setUp();
        userDao = (UserDao) context.getBean("userDao");
    }

    public void tearDown() throws Exception
    {
        userDao = null;
        super.tearDown();
    }

    public void testSaveAndLoad()
    {
        User user = new User();
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
}
