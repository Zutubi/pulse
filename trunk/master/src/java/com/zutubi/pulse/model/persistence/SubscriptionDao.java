package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.Subscription;

import java.util.List;
import java.util.Collection;

/**
 * 
 *
 */
public interface SubscriptionDao extends EntityDao<Subscription>
{
    List<Subscription> findByProject(Project project);

    List<Subscription> findByNoProject();
}
