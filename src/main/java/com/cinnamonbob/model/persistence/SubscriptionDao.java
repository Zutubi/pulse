package com.cinnamonbob.model.persistence;

import com.cinnamonbob.model.Subscription;
import com.cinnamonbob.model.Project;

import java.util.List;

/**
 * 
 *
 */
public interface SubscriptionDao extends EntityDao<Subscription>
{
    List<Subscription> findByProject(Project project);
}
