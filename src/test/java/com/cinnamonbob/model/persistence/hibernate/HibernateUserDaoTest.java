package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.User;
import com.cinnamonbob.model.EmailContactPoint;
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

        user = (User) userDao.findById(user.getId());
        assertEquals(1, user.getContactPoints().size());
        EmailContactPoint otherEmail = (EmailContactPoint) user.getContactPoints().get(0);
        assertEquals(email.getName(), otherEmail.getName());
        assertEquals(email.getEmail(), otherEmail.getEmail());
        assertEquals(user, otherEmail.getUser());
    }
}
