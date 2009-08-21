/**
 * <class-comment/>
 */
package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.persistence.TriggerDao;
import com.zutubi.pulse.master.scheduling.NoopTrigger;
import com.zutubi.pulse.master.scheduling.Trigger;

import java.util.List;

public class HibernateTriggerDaoTest extends MasterPersistenceTestCase
{
    private TriggerDao dao;

    public HibernateTriggerDaoTest(String testName)
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

    public void testCreateAndLoad()
    {
        Trigger trigger = new NoopTrigger("triggerName", "triggerGroup");
        trigger.fire();

        dao.save(trigger);

        commitAndRefreshTransaction();

        Trigger anotherTrigger = dao.findByNameAndGroup("triggerName", "triggerGroup");
        assertPropertyEquals(trigger, anotherTrigger);
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