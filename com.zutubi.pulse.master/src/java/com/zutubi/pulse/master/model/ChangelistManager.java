/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.master.security.SecureParameter;
import com.zutubi.tove.security.AccessManager;

import java.util.List;
import java.util.Set;

/**
 * Persistence manager for changelists, that is changes committed to a project's SCM.
 */
public interface ChangelistManager
{
    /**
     * Retrieves the changelist with the given id.
     *
     * @param id database id of the changelist to retrieve
     * @return the changelist with the given id, or null if there is no such changelist
     */
    PersistentChangelist getChangelist(long id);

    /**
     * Saves or updates the given changelist.
     *
     * @param changelist the changelist to save
     */
    void save(PersistentChangelist changelist);

    /**
     * Returns the most recent changelists submitted by the given user.
     *
     * @param user the user to get the changelists for
     * @param max  the maximum number of results to return
     * @return a list of up to max of the most recent changes for the user
     */
    @SecureParameter(parameterType = User.class, action = AccessManager.ACTION_VIEW)
    List<PersistentChangelist> getLatestChangesForUser(User user, int max);

    /**
     * Returns the most recent changelists that affected builds of the given project.
     *
     * @param project the project to get changes for
     * @param max     the maximum number of results to return
     * @return a list of up to max of the most recent changes that affected the given project
     */
    @SecureParameter(parameterType = Project.class, action = AccessManager.ACTION_VIEW)
    List<PersistentChangelist> getLatestChangesForProject(Project project, int max);

    /**
     * Returns the most recent changelists that affected builds of any of the given projects.
     *
     * @param projects the projects to get changes for
     * @param max      the maximum number of results to return
     * @return a list of up to max of the most recent changes that affected the given projects
     */
    @SecureParameter(parameterType = Project.class, action = AccessManager.ACTION_VIEW)
    List<PersistentChangelist> getLatestChangesForProjects(Project[] projects, int max);

    /**
     * Returns all changes associated with the given build result, optionally
     * since another earlier build.
     *
     * @param result           the result to retrieve changes for
     * @param sinceBuildNumber if greater than zero, the number of the build to
     *                         get changes since (changes in the since build
     *                         itself are not included)
     * @param allowEmpty       if true, the result may contain changelists with
     *                         no files; if false, empty changelists are
     * @return all changes associated with the given build and, if requested,
     *         earlier builds after a since marker
     */
    @SecureParameter(parameterIndex = 0, action = AccessManager.ACTION_VIEW)
    List<PersistentChangelist> getChangesForBuild(BuildResult result, long sinceBuildNumber, boolean allowEmpty);

    /**
     * Gets the number of files in the given changelist, without loading them
     * all into memory.
     *
     * @param changelist the changelist to get the size of
     * @return the number of files changed in the given changelist
     */
    int getChangelistSize(PersistentChangelist changelist);

    /**
     * Gets a page of the files changed in the given changelist.
     *
     * @param changelist changelist to get files from
     * @param offset     zero-base offset of the first file to retrieve
     * @param max        maximum number of files to retrieve
     * @return the given window from the list of files in the given changelist
     */
    List<PersistentFileChange> getChangelistFiles(PersistentChangelist changelist, int offset, int max);

    /**
     * Returns the set of all projects that had builds directly affected by the given changelist.
     *
     * @param changelist the changelist to get affected projects for
     * @return database ids of all projects with builds affected by the given changelist
     */
    Set<Long> getAffectedProjectIds(PersistentChangelist changelist);

    /**
     * Returns the set of all builds directly affected by the given changelist.
     *
     * @param changelist the changelist to get affected build for
     * @return database ids of all builds affected by the given changelist
     */
    Set<Long> getAffectedBuildIds(PersistentChangelist changelist);

    /**
     * Returns all graphs for builds affected by this change.  The roots of the graphs are builds
     * directly affected, the remainder of the graphs represent builds indirectly affected by
     * being downstream dependencies.  Note that the same build may appear in multiple graphs,
     * although it will always be represented by the same node instance (the node is shared between
     * graphs in this case).
     * 
     * @param changelist the changelist to get affected builds for
     * @return graphs of all builds affected by the change, either directly or indirectly 
     */
    List<BuildGraph> getAffectedBuilds(PersistentChangelist changelist);
    
    /**
     * Returns all new upstream changes in a build since a given build.  The upstream dependency
     * graphs of the build and since build are compared to find where new upstream builds have been
     * used.  For each new upstream build, the changes after the since upstream build are added to
     * the result.  Note that build graphs can change shape over time.  Upstream changes are only
     * identified between builds of the same project that are in the same position in the build
     * graph.
     *
     * @param build      the build to find upstream changes for
     * @param sinceBuild an earlier build of the same project used as the point after which to find
     *                   changes
     * @return all new upstream changes that may have influenced build since sinceBuild
     */
    List<UpstreamChangelist> getUpstreamChangelists(BuildResult build, BuildResult sinceBuild);
}
