package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.User;
import com.cinnamonbob.model.persistence.UserDao;

import java.util.List;

/**
 * 
 *
 * @noinspection FieldCanBeLocal
 */
public class HibernateUserDaoTest extends PersistenceTestCase
{
    private UserDao userDao;

    public void setUp() throws Exception
    {
        super.setUp();
        userDao = (UserDao) context.getBean("userDao");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSaveAndLoad()
    {
        User user = new User();
        user.setLogin("login");
        user.setName("name");
        userDao.save(user);
        commitAndRefreshTransaction();

        User anotherUser = (User) userDao.findById(user.getId());

        // want to ensure that a new object has been created by hibernate and
        // that the old one is not just returned to us.
        assertFalse(user == anotherUser);
        assertEquals(user.getLogin(), anotherUser.getLogin());
        assertEquals(user.getName(), anotherUser.getName());
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
