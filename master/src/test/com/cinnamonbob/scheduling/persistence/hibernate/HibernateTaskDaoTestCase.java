package com.cinnamonbob.scheduling.persistence.hibernate;

import com.cinnamonbob.model.persistence.hibernate.PersistenceTestCase;
import com.cinnamonbob.scheduling.NoopTask;
import com.cinnamonbob.scheduling.Task;
import com.cinnamonbob.scheduling.persistence.TaskDao;

import java.util.List;

/**
 * <class-comment/>
 */
public class HibernateTaskDaoTestCase extends PersistenceTestCase
{
    private TaskDao dao;

    public HibernateTaskDaoTestCase(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        dao = (TaskDao) context.getBean("taskDao");
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        dao = null;
        super.tearDown();
    }

    protected String[] getConfigLocations()
    {
        return new String[]{ "com/cinnamonbob/bootstrap/testBootstrapContext.xml",
            "com/cinnamonbob/scheduling/persistence/hibernate/HibernateTaskDaoContext.xml"
        };
    }

    public void testCreateAndLoad()
    {
        Task task = new NoopTask("taskName", "taskGroup");
        task.getDataMap().put("key", "value");
        task.getDataMap().put("class", HibernateTaskDao.class);

        dao.save(task);

        commitAndRefreshTransaction();

        Task anotherTask = dao.findByNameAndGroup("taskName", "taskGroup");
        assertPersistentEquals(task, anotherTask);
        assertEquals("value", anotherTask.getDataMap().get("key"));
        assertEquals(HibernateTaskDao.class, anotherTask.getDataMap().get("class"));
    }

    public void testFindByGroup()
    {
        dao.save(new NoopTask("a", "triggerGroup"));
        dao.save(new NoopTask("b", "triggerGroup"));
        dao.save(new NoopTask("c", "triggergroup"));
        dao.save(new NoopTask("d", "group"));

        commitAndRefreshTransaction();

        List<Task> tasks = dao.findByGroup("triggerGroup");
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        assertEquals(NoopTask.class, tasks.get(0).getClass());
    }
}