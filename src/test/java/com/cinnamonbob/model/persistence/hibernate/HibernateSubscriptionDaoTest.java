package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.*;
import com.cinnamonbob.model.persistence.ScmDao;
import com.cinnamonbob.model.persistence.SubscriptionDao;
import com.cinnamonbob.model.persistence.ContactPointDao;
import com.cinnamonbob.model.persistence.ProjectDao;

import java.util.List;


/**
 * <class-comment/>
 */
public class HibernateSubscriptionDaoTest extends PersistenceTestCase
{
    private SubscriptionDao subDao;
    private ProjectDao projectDao;
    private ContactPointDao contactDao;

    public HibernateSubscriptionDaoTest()
    {

    }

    public HibernateSubscriptionDaoTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        
        subDao = (SubscriptionDao) context.getBean("subscriptionDao");
        projectDao = (ProjectDao) context.getBean("projectDao");
        contactDao = (ContactPointDao) context.getBean("contactPointDao");
    }

    public void tearDown() throws Exception
    {
        // tear down here.
        
        super.tearDown();
    }

    public void testFindByProject()
    {

        ContactPoint contactPoint = new EmailContactPoint();
        contactDao.save(contactPoint);
        Project project = new Project();
        projectDao.save(project);

        Subscription subscription = new Subscription(project, contactPoint);
        subDao.save(subscription);

        commitAndRefreshTransaction();

        List<Subscription> subscriptions = subDao.findByProject(project);
        assertNotNull(subscriptions);
        assertEquals(1, subscriptions.size());
        Subscription otherSubscription = subscriptions.get(0);
        assertEquals(project, otherSubscription.getProject());
        assertEquals(contactPoint, otherSubscription.getContactPoint());
    }
}
