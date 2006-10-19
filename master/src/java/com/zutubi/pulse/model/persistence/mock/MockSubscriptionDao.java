package com.zutubi.pulse.model.persistence.mock;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectBuildSubscription;
import com.zutubi.pulse.model.Subscription;
import com.zutubi.pulse.model.persistence.SubscriptionDao;

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
                return s instanceof ProjectBuildSubscription && ((ProjectBuildSubscription) s).getProjects().contains(project);

            }
        });
    }

    public List<Subscription> findByNoProject()
    {
        return findByFilter(new Filter<Subscription>()
        {
            public boolean include(Subscription o)
            {
                return o instanceof ProjectBuildSubscription && ((ProjectBuildSubscription) o).getProjects().size() == 0;
            }
        });
    }
}
