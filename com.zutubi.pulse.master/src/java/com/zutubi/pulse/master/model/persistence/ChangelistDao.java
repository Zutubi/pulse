package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;

import java.util.List;
import java.util.Set;

/**
 * DAO for accessing Changelist objects.
 */
public interface ChangelistDao extends EntityDao<PersistentChangelist>
{
    Set<Long> findAllAffectedProjectIds(PersistentChangelist changelist);
    Set<Long> findAllAffectedResultIds(PersistentChangelist changelist);

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

    /**
     * Finds all changes associated with a given build.
     * 
     * @param id         id of the build to get the changelists for
     * @param allowEmpty if true, changelists with no file changes may be
     *                   included in the result; if false they are filtered out
     * @return the changes associated with the given build
     */
    List<PersistentChangelist> findByResult(long id, boolean allowEmpty);

    List<PersistentChangelist> findAllEquivalent(PersistentChangelist changelist);

    /**
     * Gets the number of files in the given changelist, without loading them
     * all into memory.
     * 
     * @param changelist the changelist to get the size of
     * @return the number of files changed in the given changelist
     */
    int getSize(PersistentChangelist changelist);

    /**
     * Gets a page of the files changed in the given changelist.
     * 
     * @param changelist changelist to get files from
     * @param offset     zero-base offset of the first file to retrieve
     * @param max        maximum number of files to retrieve
     * @return the given window from the list of files in the given changelist
     */
    List<PersistentFileChange> getFiles(PersistentChangelist changelist, int offset, int max);
}
