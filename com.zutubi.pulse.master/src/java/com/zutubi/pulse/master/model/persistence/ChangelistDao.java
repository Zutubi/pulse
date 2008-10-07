package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;

import java.util.List;
import java.util.Set;

/**
 * DAO for accessing Changelist objects.
 */
public interface ChangelistDao extends EntityDao<PersistentChangelist>
{
    Set<Long> getAllAffectedProjectIds(PersistentChangelist changelist);
    Set<Long> getAllAffectedResultIds(PersistentChangelist changelist);

    /**
     * Returns a list of up to max changelists submitted by the given user.
     *
     * @param user the user to restrict the query to
     * @param max  the maximum number of changelists to return
     * @return a list of the latest changes by the user
     */
    List<PersistentChangelist> findLatestByUser(User user, int max);

    /**
     * Returns a list of up to max changelists against the given project.
     *
     * @param project the project to restrict the query to
     * @param max     the maximum number fo changelists to return
     * @return a list of latest changes against the project
     */
    List<PersistentChangelist> findLatestByProject(Project project, int max);

    List<PersistentChangelist> findLatestByProjects(Project[] projects, int max);

    List<PersistentChangelist> findByResult(long id);

    List<PersistentChangelist> findAllEquivalent(PersistentChangelist changelist);
}
