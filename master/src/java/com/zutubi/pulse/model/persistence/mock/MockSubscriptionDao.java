package com.zutubi.pulse.model.persistence.mock;

import com.zutubi.pulse.model.persistence.SubscriptionDao;
import com.zutubi.pulse.model.Subscription;
import com.zutubi.pulse.model.Project;

import java.util.List;

/**
 */
public class MockSubscriptionDao extends MockEntityDao<Subscription> implements SubscriptionDao
{
    public List<Subscription> findByProject(final Project project)
    {
        return findByFilter(new Filter<Subscription>()
        {
            public boolean include(Subscription s)
            {
                for(Project p: s.getProjects())
                {
                    if(p.equals(project))
                    {
                        return true;
                    }
                }

                return false;
            }
        });
    }

    public List<Subscription> findByNoProject()
    {
        return findByFilter(new Filter<Subscription>()
        {
            public boolean include(Subscription o)
            {
                return o.getProjects().size() == 0;
            }
        });
    }
}
