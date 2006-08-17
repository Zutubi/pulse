package com.zutubi.pulse.model;

import com.zutubi.pulse.model.persistence.hibernate.MasterPersistenceTestCase;
import com.zutubi.pulse.model.persistence.SubscriptionDao;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.ContactPointDao;
import com.zutubi.pulse.model.persistence.mock.MockEntityDao;
import com.zutubi.pulse.model.persistence.mock.MockSubscriptionDao;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class SubscriptionManagerTest extends PulseTestCase
{
    private DefaultSubscriptionManager subscriptionManager;

    public SubscriptionManagerTest()
    {

    }

    public SubscriptionManagerTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        SubscriptionDao subscriptionDao = new MockSubscriptionDao();
        subscriptionManager = new DefaultSubscriptionManager();
        subscriptionManager.setSubscriptionDao(subscriptionDao);
    }

    public void tearDown() throws Exception
    {
        // tear down here.
        subscriptionManager = null;
        super.tearDown();
    }

    public void testDeleteByProjectLastProject()
    {
        ContactPoint contactPoint = new EmailContactPoint();
        Subscription subscription = new Subscription(contactPoint);
        Project project = addProject(11, subscription);
        subscriptionManager.save(subscription);

        subscriptionManager.deleteAllSubscriptions(project);

        assertNull(subscriptionManager.getSubscription(subscription.getId()));
    }

    public void testDeleteByProjectMoreProjects()
    {
        ContactPoint contactPoint = new EmailContactPoint();
        Subscription subscription = new Subscription(contactPoint);
        Project project = addProject(11, subscription);
        addProject(12, subscription);
        subscriptionManager.save(subscription);

        subscriptionManager.deleteAllSubscriptions(project);

        Subscription other = subscriptionManager.getSubscription(subscription.getId());
        assertNotNull(other);
        assertEquals(1, other.getProjects().size());
    }

    public void testDeleteByProjectNotIncluded()
    {
        ContactPoint contactPoint = new EmailContactPoint();
        Subscription subscription = new Subscription(contactPoint);
        addProject(11, subscription);
        subscriptionManager.save(subscription);

        Project project = new Project();
        project.setId(1211);
        subscriptionManager.deleteAllSubscriptions(project);

        Subscription other = subscriptionManager.getSubscription(subscription.getId());
        assertNotNull(other);
        assertEquals(1, other.getProjects().size());
    }

    private Project addProject(long id, Subscription subscription)
    {
        Project p = new Project();
        p.setId(id);
        subscription.getProjects().add(p);
        return p;
    }
}
