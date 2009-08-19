package com.zutubi.pulse.master.dependency;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.events.system.SystemStartedEvent;
import com.zutubi.tove.config.ConfigurationEventListener;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.util.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service to yield dependency graphs for projects.  Contains the building
 * logic and cached information to ensure building graphs is fast.
 */
public class ProjectDependencyGraphBuilder implements ConfigurationEventListener
{
    public enum TransitiveMode
    {
        /**
         * Show all transitive dependencies, including duplicates.
         */
        FULL,
        /**
         * Show transitive dependencies but trim the children of duplicate
         * subtree roots and mark those roots as filtered.  A single occurrence
         * of the subtree is left intact at the deepest level found.
         */
        TRIM_DUPLICATES,
        /**
         * Show transitive dependencies but completely remove duplicate
         * subtrees.  A single occurrence of the subtree is left intact at the
         * deepest level found.
         */
        REMOVE_DUPLICATES,
        /**
         * Show only a single level of dependencies.
         */
        NONE
    }

    /**
     * Maps from a project to those projects that directly depend upon it, as
     * this information is only indirectly available in the configuration.
     */
    private Map<ProjectConfiguration, List<ProjectConfiguration>> downstreamCache;
    private ReadWriteLock downstreamCacheLock = new ReentrantReadWriteLock();
    private ProjectManager projectManager;

    /**
     * Builds and returns the dependency graph for the given project.
     *
     * @param project project to build the graph for
     * @param mode    mode that determines if and how transitive dependencies
     *                are included in the result (see the enum constants for
     *                details)
     * @return a dependency graph for the given project
     */
    public ProjectDependencyGraph build(Project project, TransitiveMode mode)
    {
        return new ProjectDependencyGraph(buildUpstream(project, mode), buildDownstream(project, mode));
    }

    private TreeNode<DependencyGraphData> buildUpstream(Project project, TransitiveMode mode)
    {
        TreeNode<DependencyGraphData> node = new TreeNode<DependencyGraphData>(new DependencyGraphData(project));

        List<ProjectConfiguration> upstreamProjectConfigs = getDependentProjectConfigs(project.getConfig());
        List<Project> upstreamProjects = projectManager.mapConfigsToProjects(upstreamProjectConfigs);
        for (Project upstream: upstreamProjects)
        {
            TreeNode<DependencyGraphData> child;
            if (mode == TransitiveMode.NONE)
            {
                child = new TreeNode<DependencyGraphData>(new DependencyGraphData(upstream));
            }
            else
            {
                child = buildUpstream(upstream, mode);
            }

            node.add(child);
        }

        processDuplicateSubtrees(node, mode);
        return node;
    }

    private TreeNode<DependencyGraphData> buildDownstream(Project project, TransitiveMode mode)
    {
        downstreamCacheLock.readLock().lock();
        try
        {
            TreeNode<DependencyGraphData> node = new TreeNode<DependencyGraphData>(new DependencyGraphData(project));

            List<ProjectConfiguration> downstreamProjectConfigs = downstreamCache.get(project.getConfig());
            if (downstreamProjectConfigs != null)
            {
                List<Project> downstreamProjects = projectManager.mapConfigsToProjects(downstreamProjectConfigs);
                for (Project downstream: downstreamProjects)
                {
                    TreeNode<DependencyGraphData> child;
                    if (mode == TransitiveMode.NONE)
                    {
                        child = new TreeNode<DependencyGraphData>(new DependencyGraphData(downstream));
                    }
                    else
                    {
                        child = buildDownstream(downstream, mode);
                    }

                    node.add(child);
                }
            }

            processDuplicateSubtrees(node, mode);
            return node;
        }
        finally
        {
            downstreamCacheLock.readLock().unlock();
        }
    }

    private void processDuplicateSubtrees(TreeNode<DependencyGraphData> root, TransitiveMode mode)
    {
        if (mode == TransitiveMode.TRIM_DUPLICATES || mode == TransitiveMode.REMOVE_DUPLICATES)
        {
            final Map<Project, TreeNode<DependencyGraphData>> seenProjects = new HashMap<Project, TreeNode<DependencyGraphData>>();

            root.breadthFirstWalk(new UnaryProcedure<TreeNode<DependencyGraphData>>()
            {
                public void process(TreeNode<DependencyGraphData> node)
                {
                    TreeNode<DependencyGraphData> lastSeen = seenProjects.get(node.getData().getProject());
                    if (lastSeen != null)
                    {
                        // Seen somewhere less (or equally as) deep, filter that
                        // previous node's children.  We can safely modify that
                        // node as the walk has already visited it (and although we
                        // may needlessly walk on its children, that does no harm).
                        lastSeen.getData().markSubtreeFiltered();
                        lastSeen.clear();
                    }

                    seenProjects.put(node.getData().getProject(), node);
                }
            });

            if (mode == TransitiveMode.REMOVE_DUPLICATES)
            {
                // Remove the marked nodes themselves too.
                root.filteringWalk(new Predicate<TreeNode<DependencyGraphData>>()
                {
                    public boolean satisfied(TreeNode<DependencyGraphData> node)
                    {
                        return !node.getData().isSubtreeFiltered();
                    }
                });
            }
        }
    }

    private void refreshDownstreamCache()
    {
        downstreamCacheLock.writeLock().lock();
        try
        {
            downstreamCache = new HashMap<ProjectConfiguration, List<ProjectConfiguration>>();

            List<ProjectConfiguration> configs = projectManager.getAllProjectConfigs(true);
            for (ProjectConfiguration config: configs)
            {
                for (ProjectConfiguration upstream: getDependentProjectConfigs(config))
                {
                    addToDownstreamCache(upstream, config);
                }
            }
        }
        finally
        {
            downstreamCacheLock.writeLock().unlock();
        }
    }

    private void addToDownstreamCache(ProjectConfiguration upstream, ProjectConfiguration config)
    {
        List<ProjectConfiguration> downstreamConfigs = downstreamCache.get(upstream);
        if (downstreamConfigs == null)
        {
            downstreamConfigs = new LinkedList<ProjectConfiguration>();
            downstreamCache.put(upstream, downstreamConfigs);
        }

        downstreamConfigs.add(config);
    }

    private List<ProjectConfiguration> getDependentProjectConfigs(ProjectConfiguration config)
    {
        List<DependencyConfiguration> dependencies = config.getDependencies().getDependencies();
        return CollectionUtils.map(dependencies, new Mapping<DependencyConfiguration, ProjectConfiguration>()
        {
            public ProjectConfiguration map(DependencyConfiguration dependencyConfiguration)
            {
                return dependencyConfiguration.getProject();
            }
        });
    }

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        // When any project changes, rebuild the whole cache.  No need to be
        // smart about it - rebuilding is not that expensive.
        refreshDownstreamCache();
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(new EventListener()
        {
            public void handleEvent(Event event)
            {
                if (event instanceof ConfigurationEventSystemStartedEvent)
                {
                    ConfigurationEventSystemStartedEvent cesse = (ConfigurationEventSystemStartedEvent) event;
                    cesse.getConfigurationProvider().registerEventListener(ProjectDependencyGraphBuilder.this, false, true, ProjectConfiguration.class);
                }
                else
                {
                    refreshDownstreamCache();
                }
            }

            public Class[] getHandledEvents()
            {
                return new Class[]{ConfigurationEventSystemStartedEvent.class, SystemStartedEvent.class};
            }
        });
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
