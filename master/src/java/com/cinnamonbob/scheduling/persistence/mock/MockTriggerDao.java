package com.cinnamonbob.scheduling.persistence.mock;


import com.cinnamonbob.scheduling.Trigger;
import com.cinnamonbob.scheduling.persistence.TriggerDao;

import java.util.List;

/**
 * <class-comment/>
 */
public class MockTriggerDao extends MockEntityDao<Trigger> implements TriggerDao
{

    public List<Trigger> findByGroup(final String group)
    {
        return findByFilter(new Filter()
        {
            public boolean include(Object o)
            {
                Trigger trigger = (Trigger) o;
                return group.compareTo(trigger.getGroup()) == 0;
            }
        });
    }

    public Trigger findByNameAndGroup(final String name, final String group)
    {
        return findUniqueByFilter(new Filter()
        {
            public boolean include(Object o)
            {
                Trigger trigger = (Trigger) o;
                return group.compareTo(trigger.getGroup()) == 0 &&
                        name.compareTo(trigger.getName()) == 0;
            }
        });
    }
}
