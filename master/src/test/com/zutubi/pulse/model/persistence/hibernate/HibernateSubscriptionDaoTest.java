package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.ContactPoint;
import com.zutubi.pulse.model.EmailContactPoint;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.Subscription;
import com.zutubi.pulse.model.persistence.ContactPointDao;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.SubscriptionDao;

import java.util.List;
import java.util.LinkedList;


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
        List<Project> projects = new LinkedList<Project>();
        projects.add(project);
        Subscription subscription = new Subscription(projects, contactPoint);
        save(contactPoint, project, subscription);

        List<Subscription> subscriptions = subDao.findByProject(project);
        assertNotNull(subscriptions);
        assertEquals(1, subscriptions.size());
        Subscription otherSubscription = subscriptions.get(0);
        assertEquals(projects.get(0), otherSubscription.getProjects().get(0));
        assertEquals(contactPoint, otherSubscription.getContactPoint());
    }

    public void testFindByNoProject()
    {
        ContactPoint contactPoint = new EmailContactPoint();
        Project project = new Project();
        Subscription subscription = new Subscription(contactPoint);
        save(contactPoint, project, subscription);

        List<Subscription> subscriptions = subDao.findByNoProject();
        assertNotNull(subscriptions);
        assertEquals(1, subscriptions.size());
        Subscription otherSubscription = subscriptions.get(0);
        assertEquals(0, otherSubscription.getProjects().size());
        assertEquals(contactPoint, otherSubscription.getContactPoint());
    }

    public void testFindByNoProjectMultiple()
    {
        ContactPoint contactPoint = new EmailContactPoint();
        Project project = new Project();
        List<Project> projects = new LinkedList<Project>();
        projects.add(project);
        Subscription subWithProject = new Subscription(projects, contactPoint);
        save(contactPoint, project, subWithProject);

        Subscription subWithoutProject = new Subscription(contactPoint);
        save(subWithoutProject);
        
        List<Subscription> subscriptions = subDao.findByNoProject();
        assertNotNull(subscriptions);
        assertEquals(1, subscriptions.size());
        Subscription otherSubscription = subscriptions.get(0);
        assertEquals(0, otherSubscription.getProjects().size());
        assertEquals(subWithoutProject, otherSubscription);
    }

    public void testDeleteSubscription()
    {
        ContactPoint contactPoint = new EmailContactPoint();
        Project project = new Project();
        List<Project> projects = new LinkedList<Project>();
        projects.add(project);
        Subscription subscription = new Subscription(projects, contactPoint);
        save(contactPoint, project, subscription);

        subDao.delete(subscription);
        commitAndRefreshTransaction();

        assertNotNull(contactDao.findById(contactPoint.getId()));
        assertNotNull(projectDao.findById(project.getId()));
        assertNull(subDao.findById(subscription.getId()));
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
