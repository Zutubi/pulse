package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.bootstrap.DBBootstrap;
import com.cinnamonbob.model.User;
import com.cinnamonbob.model.persistence.UserDao;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

/**
 * 
 *
 * @noinspection FieldCanBeLocal
 */
public class HibernateUserDaoTest extends TestCase
{
    private ApplicationContext context = null;
    
    private UserDao userDao;
    
    private PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;
    private DefaultTransactionDefinition transactionDefinition;
    
    public void setUp() throws Exception
    {
        String[] configLocations = new String[]{
            "com/cinnamonbob/bootstrap/testApplicationContext.xml"
        };
        context = new ClassPathXmlApplicationContext(configLocations);
        userDao = (UserDao) context.getBean("userDao");
        
        DBBootstrap dbBootstrap = new DBBootstrap(context);
        dbBootstrap.initialiseDatabase();

        transactionManager = (PlatformTransactionManager)context.getBean("transactionManager");
        transactionDefinition = new DefaultTransactionDefinition(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
        transactionStatus = transactionManager.getTransaction(transactionDefinition);        
    }
    
    public void tearDown() throws Exception
    {
        transactionManager.commit(transactionStatus);        
        context = null;
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

    protected void commitAndRefreshTransaction()
    {
        transactionManager.commit(transactionStatus);
        transactionStatus = transactionManager.getTransaction(transactionDefinition);
    }
}
