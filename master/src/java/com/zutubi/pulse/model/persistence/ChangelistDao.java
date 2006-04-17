/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.User;

import java.util.List;

/**
 * DAO for accessing Changelist objects.
 */
public interface ChangelistDao extends EntityDao<Changelist>
{
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

    Changelist findByRevision(Revision revision);
}
