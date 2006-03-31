package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.model.EmailContactPoint;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Subscription;
import com.cinnamonbob.model.persistence.ContactPointDao;
import com.cinnamonbob.model.persistence.ProjectDao;
import com.cinnamonbob.model.persistence.SubscriptionDao;

import java.util.List;


/**
 * <class-comment/>
 */
public class HibernateSubscriptionDaoTest extends MasterPersistenceTestCase
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
        subDao = null;
        projectDao = null;
        contactDao = null;

        super.tearDown();
    }

    public void testFindByProject()
    {
        ContactPoint contactPoint = new EmailContactPoint();
        Project project = new Project();
        Subscription subscription = new Subscription(project, contactPoint);
        save(contactPoint, project, subscription);

        List<Subscription> subscriptions = subDao.findByProject(project);
        assertNotNull(subscriptions);
        assertEquals(1, subscriptions.size());
        Subscription otherSubscription = subscriptions.get(0);
        assertEquals(project, otherSubscription.getProject());
        assertEquals(contactPoint, otherSubscription.getContactPoint());
    }

    public void testDeleteSubscription()
    {
        ContactPoint contactPoint = new EmailContactPoint();
        Project project = new Project();
        Subscription subscription = new Subscription(project, contactPoint);
        save(contactPoint, project, subscription);

        subDao.delete(subscription);
        commitAndRefreshTransaction();

        assertNotNull(contactDao.findById(contactPoint.getId()));
        assertNotNull(projectDao.findById(project.getId()));
        assertNull(subDao.findById(subscription.getId()));
    }

    public void testDeleteByProject()
    {
        ContactPoint contactPoint = new EmailContactPoint();
        Project project = new Project();
        Subscription subscription = new Subscription(project, contactPoint);
        save(contactPoint, project, subscription);

        Subscription subscriptionB = new Subscription(project, contactPoint);
        save(subscriptionB);

        assertEquals(2, subDao.deleteByProject(project));
        commitAndRefreshTransaction();

        assertNull(subDao.findById(subscription.getId()));
        assertNull(subDao.findById(subscriptionB.getId()));
    }

    private void save(ContactPoint c, Project p, Subscription s)
    {
        contactDao.save(c);
        projectDao.save(p);
        subDao.save(s);
        commitAndRefreshTransaction();
    }

    private void save(Subscription s)
    {
        subDao.save(s);
        commitAndRefreshTransaction();
    }
}
