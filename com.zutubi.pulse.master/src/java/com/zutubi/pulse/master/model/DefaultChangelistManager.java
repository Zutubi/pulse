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

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.master.model.persistence.ChangelistDao;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.adt.DAGraph;
import org.springframework.security.access.AccessDeniedException;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.find;

/**
 * The default implementation of {@link ChangelistManager}.
 */
public class DefaultChangelistManager implements ChangelistManager
{
    private ChangelistDao changelistDao;
    private BuildManager buildManager;
    private DependencyManager dependencyManager;

    public void save(PersistentChangelist changelist)
    {
        changelistDao.save(changelist);
    }

    public PersistentChangelist getChangelist(long id)
    {
        return changelistDao.findById(id);
    }

    public List<PersistentChangelist> getLatestChangesForUser(User user, int max)
    {
        return changelistDao.findLatestByUser(user, max);
    }

    public List<PersistentChangelist> getLatestChangesForProject(Project project, int max)
    {
        return changelistDao.findLatestByProject(project, max);
    }

    public List<PersistentChangelist> getLatestChangesForProjects(Project[] projects, int max)
    {
        return changelistDao.findLatestByProjects(projects, max);
    }

    public List<PersistentChangelist> getChangesForBuild(BuildResult result, long sinceBuildNumber, boolean allowEmpty)
    {
        List<PersistentChangelist> changelists = new LinkedList<PersistentChangelist>();

        // Get changes for all results after since, up to and including to.
        if (sinceBuildNumber > 0 && sinceBuildNumber < result.getNumber() - 1)
        {
            List<BuildResult> resultRange = buildManager.queryBuilds(result.getProject(), ResultState.getCompletedStates(), sinceBuildNumber + 1, result.getNumber() - 1, 0, -1, true, false);
            for(BuildResult r: resultRange)
            {
                changelists.addAll(changelistDao.findByResult(r.getId(), allowEmpty));
            }
        }

        changelists.addAll(changelistDao.findByResult(result.getId(), allowEmpty));
        return changelists;
    }

    public int getChangelistSize(PersistentChangelist changelist)
    {
        return changelistDao.getSize(changelist);
    }

    public List<PersistentFileChange> getChangelistFiles(PersistentChangelist changelist, int offset, int max)
    {
        return changelistDao.getFiles(changelist, offset, max);
    }

    public Set<Long> getAffectedProjectIds(PersistentChangelist changelist)
    {
        return changelistDao.findAllAffectedProjectIds(changelist);
    }

    public Set<Long> getAffectedBuildIds(PersistentChangelist changelist)
    {
        return changelistDao.findAllAffectedResultIds(changelist);
    }

    public List<BuildGraph> getAffectedBuilds(PersistentChangelist changelist)
    {
        List<BuildGraph> graphs = new LinkedList<BuildGraph>();
        for (Long directlyAffectedId: getAffectedBuildIds(changelist))
        {
            // Check the graphs we've created thus far to see if the build is already represented.
            // If so, we reuse that node as the root of this graph.
            DAGraph.Node<BuildResult> existingNode = findNode(graphs, directlyAffectedId);
            if (existingNode == null)
            {
                try
                {
                    BuildResult directlyAffected = buildManager.getBuildResult(directlyAffectedId);
                    graphs.add(dependencyManager.getDownstreamDependencyGraph(directlyAffected));
                }
                catch (AccessDeniedException e)
                {
                    // Ignore builds the user cannot access.
                }
            }
            else
            {
                graphs.add(new BuildGraph(existingNode));
            }
        }
        
        return graphs;
    }

    private DAGraph.Node<BuildResult> findNode(List<BuildGraph> graphs, Long buildId)
    {
        for (BuildGraph graph: graphs)
        {
            DAGraph.Node<BuildResult> node = graph.findNodeByBuildId(buildId);
            if (node != null)
            {
                return node;
            }
        }
        
        return null;
    }

    public List<UpstreamChangelist> getUpstreamChangelists(final BuildResult build, BuildResult sinceBuild)
    {
        final List<UpstreamChangelist> upstreamChangelists = new LinkedList<UpstreamChangelist>();
        if (sinceBuild != null)
        {
            final BuildGraph buildGraph = dependencyManager.getUpstreamDependencyGraph(build);
            final BuildGraph sinceGraph = dependencyManager.getUpstreamDependencyGraph(sinceBuild);

            buildGraph.forEach(new UnaryProcedure<DAGraph.Node<BuildResult>>()
            {
                public void run(DAGraph.Node<BuildResult> node)
                {
                    if (node.getData() == build)
                    {
                        // Skip the root.
                        return;
                    }

                    Set<BuildPath> buildPaths = buildGraph.getBuildPaths(node);
                    for (BuildPath buildPath: buildPaths)
                    {
                        DAGraph.Node<BuildResult> sinceNode = sinceGraph.findNodeByProjects(buildPath);
                        if (sinceNode != null && sinceNode.getData().getId() != node.getData().getId())
                        {
                            // A different build of the project was upstream last time, get changes since
                            // that build.
                            addUpstreamChangesForPath(buildPath, node, sinceNode, upstreamChangelists);
                        }
                    }
                }
            });
        }

        return upstreamChangelists;
    }

    private void addUpstreamChangesForPath(BuildPath buildPath, DAGraph.Node<BuildResult> node, DAGraph.Node<BuildResult> sinceNode, List<UpstreamChangelist> upstreamChangelists)
    {
        List<PersistentChangelist> changelists = getChangesForBuild(node.getData(), sinceNode.getData().getNumber(), true);
        for (final PersistentChangelist changelist: changelists)
        {
            UpstreamChangelist upstreamChangelist = find(upstreamChangelists, new Predicate<UpstreamChangelist>()
            {
                public boolean apply(UpstreamChangelist upstreamChange)
                {
                    return upstreamChange.getChangelist().isEquivalent(changelist);
                }
            }, null);

            if (upstreamChangelist == null)
            {
                upstreamChangelist = new UpstreamChangelist(changelist, buildPath);
                upstreamChangelists.add(upstreamChangelist);
            }
            else
            {
                // We've seen this change before, just add another context path.
                upstreamChangelist.addUpstreamContext(buildPath);
            }
        }
    }

    public void setChangelistDao(ChangelistDao changelistDao)
    {
        this.changelistDao = changelistDao;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setDependencyManager(DependencyManager dependencyManager)
    {
        this.dependencyManager = dependencyManager;
    }
}
