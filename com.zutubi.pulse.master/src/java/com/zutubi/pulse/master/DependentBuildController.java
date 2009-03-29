package com.zutubi.pulse.master;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The dependent build controller is reponsible for handling the organisation of
 * builds amongst dependent projects.
 */
public class DependentBuildController implements EventListener
{
    private static final Class[] HANDLED_EVENTS = new Class[]{
            BuildCompletedEvent.class
    };

    /**
     * The pulse event manager, source of the input events.
     */
    private EventManager eventManager;

    private ConfigurationProvider configurationProvider;

    private ProjectManager projectManager;

    public void init()
    {
        eventManager.register(this);
    }

    public void handleEvent(Event event)
    {
        BuildCompletedEvent buildCompletedEvent = (BuildCompletedEvent) event;
        BuildResult result = buildCompletedEvent.getBuildResult();
        if (result.succeeded())
        {
            buildCompleted(result.getProject());
        }
    }

    private void buildCompleted(Project builtProject)
    {
        final ProjectConfiguration builtProjectConfig = builtProject.getConfig();

        // find the project dependencies that link to the built project.
        List<DependencyConfiguration> dependencies = findAll(DependencyConfiguration.class).where(new Predicate<DependencyConfiguration>()
        {
            public boolean satisfied(DependencyConfiguration dependency)
            {
                ProjectConfiguration config = dependency.getProject();
                return config.equals(builtProjectConfig);
            }
        });

        // for each of the located dependencies, find the project it belongs
        // to and request a build. 
        for (DependencyConfiguration dependency : dependencies)
        {
            ProjectConfiguration dependentProject = configurationProvider.getAncestorOfType(dependency, ProjectConfiguration.class);

            requestBuild(builtProjectConfig, dependentProject);
        }
    }

    private void requestBuild(ProjectConfiguration builtProjectConfig, ProjectConfiguration dependentProject)
    {
        // trigger build request for this dependent project.
        Project project = projectManager.getProject(dependentProject.getProjectId(), false);

        if (projectManager.isProjectValid(project))
        {
            BuildReason reason = new DependencyBuildReason(builtProjectConfig.getName());
            String source = "dependency of " + dependentProject.getName();
            projectManager.triggerBuild(dependentProject, Collections.<ResourcePropertyConfiguration>emptyList(), reason, null, source, true, false);
        }
    }

    private <T extends Configuration> Filter<T> findAll(Class<T> config)
    {
        return new Filter<T>(configurationProvider.getAll(config));
    }

    public Class[] getHandledEvents()
    {
        return HANDLED_EVENTS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    /**
     * A simple class that provides a little bit of syntactic sugar when retrieving specific
     * results.  Ie: findAll(*.class).where(predicate);
     *
     * @param <T>   is the type being handled by this filter.
     */
    private class Filter<T>
    {
        private Collection<T> collection;

        public Filter(Collection<T> collection)
        {
            this.collection = collection;
        }

        public List<T> where(Predicate<T> predicate)
        {
            return CollectionUtils.filter(collection, predicate);
        }
    }
}
