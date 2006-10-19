package com.zutubi.pulse.model;

import com.zutubi.pulse.model.persistence.SubscriptionDao;
import com.zutubi.pulse.model.persistence.mock.MockSubscriptionDao;
import com.zutubi.pulse.test.PulseTestCase;

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
        ProjectBuildSubscription subscription = new ProjectBuildSubscription(contactPoint, "html-email", "true");
        Project project = addProject(11, subscription);
        subscriptionManager.save(subscription);

        subscriptionManager.deleteAllSubscriptions(project);

        assertNull(subscriptionManager.getSubscription(subscription.getId()));
    }

    public void testDeleteByProjectMoreProjects()
    {
        ContactPoint contactPoint = new EmailContactPoint();
        ProjectBuildSubscription subscription = new ProjectBuildSubscription(contactPoint, "html-email", "true");
        Project project = addProject(11, subscription);
        addProject(12, subscription);
        subscriptionManager.save(subscription);

        subscriptionManager.deleteAllSubscriptions(project);

        ProjectBuildSubscription other = (ProjectBuildSubscription) subscriptionManager.getSubscription(subscription.getId());
        assertNotNull(other);
        assertEquals(1, other.getProjects().size());
    }

    public void testDeleteByProjectNotIncluded()
    {
        ContactPoint contactPoint = new EmailContactPoint();
        ProjectBuildSubscription subscription = new ProjectBuildSubscription(contactPoint, "html-email", "true");
        addProject(11, subscription);
        subscriptionManager.save(subscription);

        Project project = new Project();
        project.setId(1211);
        subscriptionManager.deleteAllSubscriptions(project);

        ProjectBuildSubscription other = (ProjectBuildSubscription) subscriptionManager.getSubscription(subscription.getId());
        assertNotNull(other);
        assertEquals(1, other.getProjects().size());
    }

    private Project addProject(long id, ProjectBuildSubscription subscription)
    {
        Project p = new Project();
        p.setId(id);
        subscription.getProjects().add(p);
        return p;
    }
}
