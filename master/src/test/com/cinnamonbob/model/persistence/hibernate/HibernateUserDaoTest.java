package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.EmailContactPoint;
import com.cinnamonbob.model.User;
import com.cinnamonbob.model.GrantedAuthority;
import com.cinnamonbob.model.persistence.UserDao;

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

    public void testGrantedAuthorities()
    {
        User user = new User();
        userDao.save(user);

        GrantedAuthority authority = new GrantedAuthority();
        authority.setAuthority("SUPER_USER");
        user.add(authority);
        userDao.save(user);
        commitAndRefreshTransaction();

        user = userDao.findById(user.getId());
        assertEquals(1, user.getAuthorities().size());
        GrantedAuthority otherAuthority = user.getAuthorities().get(0);
        assertPropertyEquals(authority, otherAuthority);
    }
}
