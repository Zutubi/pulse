package com.cinnamonbob.model.persistence;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Subscription;

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
