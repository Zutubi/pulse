/**
 * <class-comment/>
 */
package com.cinnamonbob.scheduling.persistence.hibernate;

import com.cinnamonbob.model.persistence.hibernate.MasterPersistenceTestCase;
import com.cinnamonbob.scheduling.NoopTrigger;
import com.cinnamonbob.scheduling.Trigger;
import com.cinnamonbob.scheduling.persistence.TriggerDao;

import java.util.List;

public class HibernateTriggerDaoTestCase extends MasterPersistenceTestCase
{
    private TriggerDao dao;

    public HibernateTriggerDaoTestCase(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        dao = (TriggerDao) context.getBean("triggerDao");
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        dao = null;
        super.tearDown();
    }

    protected String[] getConfigLocations()
    {
        return new String[]{"com/cinnamonbob/bootstrap/testBootstrapContext.xml",
                "com/cinnamonbob/scheduling/persistence/hibernate/HibernateTriggerDaoContext.xml"
        };
    }

    public void testCreateAndLoad()
    {
        Trigger trigger = new NoopTrigger("triggerName", "triggerGroup");
        trigger.getDataMap().put("key", "value");
        trigger.getDataMap().put("class", HibernateTriggerDao.class);
        trigger.fire();

        dao.save(trigger);

        commitAndRefreshTransaction();

        Trigger anotherTrigger = dao.findByNameAndGroup("triggerName", "triggerGroup");
        assertPropertyEquals(trigger, anotherTrigger);
        assertEquals("value", anotherTrigger.getDataMap().get("key"));
        assertEquals(HibernateTriggerDao.class, anotherTrigger.getDataMap().get("class"));
    }

    public void testFindByGroup()
    {
        dao.save(new NoopTrigger("a", "triggerGroup"));
        dao.save(new NoopTrigger("b", "triggerGroup"));
        dao.save(new NoopTrigger("c", "triggergroup"));
        dao.save(new NoopTrigger("d", "group"));

        commitAndRefreshTransaction();

        List<Trigger> triggers = dao.findByGroup("triggerGroup");
        assertNotNull(triggers);
        assertEquals(2, triggers.size());
        assertEquals(NoopTrigger.class, triggers.get(0).getClass());
    }
}