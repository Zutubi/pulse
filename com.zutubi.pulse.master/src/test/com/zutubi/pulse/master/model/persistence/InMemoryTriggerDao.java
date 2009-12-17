package com.zutubi.pulse.master.model.persistence;


import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.util.Predicate;

import java.util.List;

public class InMemoryTriggerDao extends InMemoryEntityDao<Trigger> implements TriggerDao
{
    public List<Trigger> findByGroup(final String group)
    {
        return findByPredicate(new Predicate<Trigger>()
        {
            public boolean satisfied(Trigger trigger)
            {
                return group.compareTo(trigger.getGroup()) == 0;
            }
        });
    }

    public Trigger findByNameAndGroup(final String name, final String group)
    {
        return findUniqueByPredicate(new Predicate<Trigger>()
        {
            public boolean satisfied(Trigger trigger)
            {
                return group.compareTo(trigger.getGroup()) == 0 &&
                        name.compareTo(trigger.getName()) == 0;
            }
        });
    }

    public List<Trigger> findByProject(final long id)
    {
        return findByPredicate(new Predicate<Trigger>()
        {
            public boolean satisfied(Trigger trigger)
            {
                return trigger.getProject() == id;
            }
        });
    }

    public Trigger findByProjectAndName(final long id, final String name)
    {
        return findUniqueByPredicate(new Predicate<Trigger>()
        {
            public boolean satisfied(Trigger trigger)
            {
                return name.compareTo(trigger.getName()) == 0 &&
                        id == trigger.getProject();
            }
        });
    }
}
