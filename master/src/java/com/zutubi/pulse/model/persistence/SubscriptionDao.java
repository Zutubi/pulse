/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.Subscription;

import java.util.List;

/**
 * 
 *
 */
public interface SubscriptionDao extends EntityDao<Subscription>
{
    List<Subscription> findByProject(Project project);

    /**
     * Delete all of the subscriptions associated with the specified project.
     *
     * @param project
     *
     * @return the number of subscriptions deleted.
     */
    int deleteByProject(Project project);
}
