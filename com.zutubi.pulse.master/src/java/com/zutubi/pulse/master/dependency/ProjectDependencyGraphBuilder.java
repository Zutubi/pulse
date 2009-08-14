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
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.TreeNode;

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
     * @return a dependency graph for the given project
     */
    public ProjectDependencyGraph build(Project project)
    {
        return new ProjectDependencyGraph(buildUpstream(project), buildDownstream(project));
    }

    private TreeNode<Project> buildUpstream(Project project)
    {
        TreeNode<Project> node = new TreeNode<Project>(project);

        List<ProjectConfiguration> upstreamProjectConfigs = getDependentProjectConfigs(project.getConfig());
        List<Project> upstreamProjects = projectManager.mapConfigsToProjects(upstreamProjectConfigs);
        for (Project upstream: upstreamProjects)
        {
            node.add(buildUpstream(upstream));
        }

        return node;
    }

    private TreeNode<Project> buildDownstream(Project project)
    {
        downstreamCacheLock.readLock().lock();
        try
        {
            TreeNode<Project> node = new TreeNode<Project>(project);

            List<ProjectConfiguration> downstreamProjectConfigs = downstreamCache.get(project.getConfig());
            if (downstreamProjectConfigs != null)
            {
                List<Project> downstreamProjects = projectManager.mapConfigsToProjects(downstreamProjectConfigs);
                for (Project downstream: downstreamProjects)
                {
                    node.add(buildDownstream(downstream));
                }
            }

            return node;
        }
        finally
        {
            downstreamCacheLock.readLock().unlock();
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
