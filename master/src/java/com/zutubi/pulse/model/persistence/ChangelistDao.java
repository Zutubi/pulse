package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.User;

import java.util.List;
import java.util.Set;

/**
 * DAO for accessing Changelist objects.
 */
public interface ChangelistDao extends EntityDao<Changelist>
{
    Set<Long> getAllAffectedProjectIds(Changelist changelist);
    Set<Long> getAllAffectedResultIds(Changelist changelist);

    /**
     * Returns a list of up to max changelists submitted by the given user.
     *
     * @param user the user to restrict the query to
     * @param max  the maximum number of changelists to return
     * @return a list of the latest changes by the user
     */
    List<Changelist> findLatestByUser(User user, int max);

    /**
     * Returns a list of up to max changelists against the given project.
     *
     * @param project the project to restrict the query to
     * @param max     the maximum number fo changelists to return
     * @return a list of latest changes against the project
     */
    List<Changelist> findLatestByProject(Project project, int max);

    List<Changelist> findLatestByProjects(Project[] projects, int max);

    List<Changelist> findByResult(long id);

    List<Changelist> findByRevision(String serverUid, Revision revision);
}
