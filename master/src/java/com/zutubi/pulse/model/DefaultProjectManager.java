package com.zutubi.pulse.model;

import com.zutubi.prototype.config.*;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.prototype.security.Actor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.NamedConfigurationComparator;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.model.TestCaseIndex;
import com.zutubi.pulse.core.scm.DelegateScmClientFactory;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.events.build.PersonalBuildRequestEvent;
import com.zutubi.pulse.events.build.RecipeDispatchedEvent;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.license.authorisation.AddProjectAuthorisation;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.TestCaseIndexDao;
import com.zutubi.pulse.model.persistence.AgentStateDao;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.LabelConfiguration;
import com.zutubi.pulse.prototype.config.group.AbstractGroupConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectAclConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.project.types.TypeConfiguration;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;
import com.zutubi.util.logging.Logger;

import java.util.*;

/**
 * 
 *
 */
public class DefaultProjectManager implements ProjectManager, ConfigurationInjector.ConfigurationSetter<Project>, EventListener
{
    private static final Logger LOG = Logger.getLogger(DefaultProjectManager.class);

    public static final int DEFAULT_WORK_DIR_BUILDS = 10;

    private ProjectDao projectDao;
    private TestCaseIndexDao testCaseIndexDao;
    private Scheduler scheduler;
    private BuildManager buildManager;
    private EventManager eventManager;
    private ChangelistIsolator changelistIsolator;
    private DelegateScmClientFactory scmClientManager;
    private LicenseManager licenseManager;
    private AgentStateDao agentStateDao;

    private ConfigurationProvider configurationProvider;
    private TypeRegistry typeRegistry;
    private ConfigurationTemplateManager configurationTemplateManager;
    private AccessManager accessManager;

    private Map<String, ProjectConfiguration> nameToConfig = new HashMap<String, ProjectConfiguration>();
    private Map<Long, ProjectConfiguration> idToConfig = new HashMap<Long, ProjectConfiguration>();
    private List<ProjectConfiguration> validConfigs = new LinkedList<ProjectConfiguration>();
    private Map<String, Set<ProjectConfiguration>> labelToConfigs = new HashMap<String, Set<ProjectConfiguration>>();

    public void initialise()
    {
        changelistIsolator = new ChangelistIsolator(buildManager);
        changelistIsolator.setScmClientFactory(scmClientManager);

        // register the canAddProject authorisation with the license manager.
        AddProjectAuthorisation addProjectAuthorisation = new AddProjectAuthorisation();
        addProjectAuthorisation.setProjectManager(this);
        licenseManager.addAuthorisation(addProjectAuthorisation);

        TypeListener<ProjectConfiguration> listener = new TypeListener<ProjectConfiguration>(ProjectConfiguration.class)
        {
            public void postInsert(ProjectConfiguration instance)
            {
                Project project = new Project();
                save(project);
                instance.setProjectId(project.getId());
                registerProjectConfig(instance);
            }

            public void postDelete(ProjectConfiguration instance)
            {
                nameToConfig.remove(instance.getName());
                idToConfig.remove(instance.getProjectId());
                validConfigs.remove(instance);
                removeFromLabelMap(instance);
            }

            public void postSave(ProjectConfiguration instance)
            {
                // Tricky: the name may have changed.
                ProjectConfiguration old = idToConfig.remove(instance.getProjectId());
                if(old != null)
                {
                    nameToConfig.remove(old.getName());
                    validConfigs.remove(old);
                    removeFromLabelMap(old);
                }

                registerProjectConfig(instance);
            }
        };
        listener.register(configurationProvider);
        updateProjects();

        // create default project if it is required.
        ensureDefaultProjectDefined();
    }

    private void ensureDefaultProjectDefined()
    {
        if (DefaultSetupManager.initialInstallation)
        {
            try
            {
                ProjectConfiguration globalProject = new ProjectConfiguration();
                globalProject.setName(GLOBAL_PROJECT_NAME);
                globalProject.setDescription("The global template is the base of the project template hierarchy.  Configuration shared among all projects should be added here.");
                globalProject.setPermanent(true);
                
                // All users can view all projects by default.
                AbstractGroupConfiguration group = configurationProvider.get(PathUtils.getPath(ConfigurationRegistry.GROUPS_SCOPE, UserManager.ALL_USERS_GROUP_NAME), AbstractGroupConfiguration.class);
                globalProject.addPermission(new ProjectAclConfiguration(group, AccessManager.ACTION_VIEW));

                // Anonymous users can view all projects by default (but only
                // when anonymous access is explicitly enabled).
                group = configurationProvider.get(PathUtils.getPath(ConfigurationRegistry.GROUPS_SCOPE, UserManager.ANONYMOUS_USERS_GROUP_NAME), AbstractGroupConfiguration.class);
                globalProject.addPermission(new ProjectAclConfiguration(group, AccessManager.ACTION_VIEW));
                
                // Project admins can do just that
                group = configurationProvider.get(PathUtils.getPath(ConfigurationRegistry.GROUPS_SCOPE, UserManager.PROJECT_ADMINS_GROUP_NAME), AbstractGroupConfiguration.class);
                globalProject.addPermission(new ProjectAclConfiguration(group, AccessManager.ACTION_ADMINISTER));

                CompositeType projectType = typeRegistry.getType(ProjectConfiguration.class);
                MutableRecord globalTemplate = projectType.unstantiate(globalProject);
                configurationTemplateManager.markAsTemplate(globalTemplate);
                configurationTemplateManager.insertRecord(ConfigurationRegistry.PROJECTS_SCOPE, globalTemplate);
            }
            catch (TypeException e)
            {
                LOG.severe("Unable to create global project template: " + e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private void updateProjects()
    {
        for(ProjectConfiguration config: configurationProvider.getAll(ProjectConfiguration.class))
        {
            registerProjectConfig(config);
        }
    }

    private void registerProjectConfig(ProjectConfiguration projectConfig)
    {
        nameToConfig.put(projectConfig.getName(), projectConfig);
        idToConfig.put(projectConfig.getProjectId(), projectConfig);
        if(configurationTemplateManager.isDeeplyValid(projectConfig.getConfigurationPath()))
        {
            validConfigs.add(projectConfig);

            for(LabelConfiguration label: projectConfig.getLabels())
            {
                Set<ProjectConfiguration> projects = labelToConfigs.get(label.getLabel());
                if(projects == null)
                {
                    projects = new TreeSet<ProjectConfiguration>(new NamedConfigurationComparator());
                    labelToConfigs.put(label.getLabel(), projects);
                }

                projects.add(projectConfig);
            }
        }
    }

    private void removeFromLabelMap(ProjectConfiguration projectConfig)
    {
        for(LabelConfiguration label: projectConfig.getLabels())
        {
            Set<ProjectConfiguration> projects = labelToConfigs.get(label.getLabel());
            if(projects != null)
            {
                projects.remove(projectConfig);
                if(projects.size() == 0)
                {
                    labelToConfigs.remove(label.getLabel());
                }
            }
        }
    }

    public void save(Project project)
    {
        projectDao.save(project);
    }

    public List<ProjectConfiguration> getAllProjectConfigs(boolean allowInvalid)
    {
        if(allowInvalid)
        {
            return Collections.unmodifiableList(new LinkedList<ProjectConfiguration>(nameToConfig.values()));
        }
        else
        {
            return Collections.unmodifiableList(validConfigs);
        }
    }

    public ProjectConfiguration getProjectConfig(String name, boolean allowInvalid)
    {
        return checkValidity(nameToConfig.get(name), allowInvalid);
    }

    public ProjectConfiguration getProjectConfig(long id, boolean allowInvalid)
    {
        return checkValidity(idToConfig.get(id), allowInvalid);
    }

    private ProjectConfiguration checkValidity(ProjectConfiguration configuration, boolean allowInvalid)
    {
        if(!allowInvalid && configuration != null && !configurationTemplateManager.isDeeplyValid(configuration.getConfigurationPath()))
        {
            return null;
        }
        return configuration;
    }

    public Project getProject(String name, boolean allowInvalid)
    {
        ProjectConfiguration config = nameToConfig.get(name);
        if(config == null || !allowInvalid && !configurationTemplateManager.isDeeplyValid(config.getConfigurationPath()))
        {
            return null;
        }
        return projectDao.findById(config.getProjectId());
    }

    public Project getProject(long id, boolean allowInvalid)
    {
        Project project = projectDao.findById(id);
        if(!allowInvalid && project != null && !configurationTemplateManager.isDeeplyValid(project.getConfig().getConfigurationPath()))
        {
            return null;
        }
        return project;
    }

    public List<Project> getProjects(boolean allowInvalid)
    {
        return filterValidProjects(projectDao.findAll());
    }

    private List<Project> filterValidProjects(List<Project> projects)
    {
        return CollectionUtils.filter(projects, new Predicate<Project>()
        {
            public boolean satisfied(Project project)
            {
                return project.getConfig() != null && configurationTemplateManager.isDeeplyValid(project.getConfig().getConfigurationPath());
            }
        });
    }

    public int getProjectCount()
    {
        return projectDao.count();
    }

    private void deleteProject(Project entity)
    {
        try
        {
            scheduler.unscheduleAllTriggers(entity.getId());
        }
        catch (SchedulingException e)
        {
            LOG.warning("Unable to unschedule triggers for project '" + entity.getId() + "'", e);
        }
        
        buildManager.deleteAllBuilds(entity);

        // Remove test case index
        List<TestCaseIndex> tests;
        do
        {
            tests = testCaseIndexDao.findByProject(entity.getId(), 100);
            for(TestCaseIndex index: tests)
            {
                testCaseIndexDao.delete(index);
            }
        }
        while(tests.size() > 0);
        
        projectDao.delete(entity);
    }

    public void delete(Project project)
    {
        project = getProject(project.getId(), true);
        if (project != null)
        {
            deleteProject(project);
        }

        licenseManager.refreshAuthorisations();
    }

    public void checkWrite(Project project)
    {
    }

    public void triggerBuild(ProjectConfiguration projectConfig, BuildReason reason, Revision revision, boolean force)
    {
        Project project = getProject(projectConfig.getProjectId(), false);

        if(revision == null)
        {
            if(projectConfig.getOptions().getIsolateChangelists())
            {
                // In this case we need to check if there are multiple
                // outstanding revisions and if so create requests for each one.
                try
                {
                    List<Revision> revisions = changelistIsolator.getRevisionsToRequest(projectConfig, project, force);
                    for(Revision r: revisions)
                    {
                        requestBuildOfRevision(reason, project, r);
                    }
                }
                catch (ScmException e)
                {
                    LOG.error("Unable to determine revisions to build for project '" + projectConfig.getName() + "': " + e.getMessage(), e);
                }
            }
            else
            {
                eventManager.publish(new BuildRequestEvent(this, reason, project, new BuildRevision()));
            }
        }
        else
        {
            // Just raise one request.
            requestBuildOfRevision(reason, project, revision);
        }
    }

    public void triggerBuild(long number, Project project, User user, PatchArchive archive) throws PulseException
    {
        ProjectConfiguration projectConfig = getProjectConfig(project.getId(), false);
        if(projectConfig == null)
        {
            return;
        }

        Revision revision = archive.getStatus().getRevision();
        try
        {
            String pulseFile = getPulseFile(projectConfig, revision, archive);
            eventManager.publish(new PersonalBuildRequestEvent(this, number, new BuildRevision(revision, pulseFile, false), user, archive, projectConfig));
        }
        catch (Exception e)
        {
            throw new PulseException(e.getMessage(), e);
        }
    }

    public long getNextBuildNumber(Project project)
    {
        project = getProject(project.getId(), true);
        long number = project.getNextBuildNumber();
        project.setNextBuildNumber(number + 1);
        save(project);
        return number;
    }

    private void requestBuildOfRevision(BuildReason reason, Project project, Revision revision)
    {
        try
        {
            String pulseFile = getPulseFile(project.getConfig(), revision, null);
            eventManager.publish(new BuildRequestEvent(this, reason, project, new BuildRevision(revision, pulseFile, reason.isUser())));
        }
        catch (Exception e)
        {
            LOG.severe("Unable to obtain pulse file for project '" + project.getName() + "', revision '" + revision.getRevisionString() + "': " + e.getMessage(), e);
        }
    }

    private String getPulseFile(ProjectConfiguration projectConfig, Revision revision, PatchArchive patch) throws Exception
    {
        TypeConfiguration type = projectConfig.getType();
        ComponentContext.autowire(type);
        return type.getPulseFile(0, projectConfig, revision, patch);
    }

/*
    // FIXME: Need to apply this logic to the new configuration, the trigger stuff in particular will
    // need to be checked.
    public void deleteBuildSpecification(Project project, long specId)
    {
        project = projectDao.findById(project.getId());
        BuildSpecification spec = project.getBuildSpecification(specId);

        if (spec == null)
        {
            throw new IllegalArgumentException("Unknown build specification [" + specId + "]");
        }

        if(project.getDefaultSpecification().equals(spec))
        {
            throw new IllegalArgumentException("The default build specification cannot be deleted");
        }

        List<Trigger> triggers = triggerDao.findByProject(project.getId());
        for (Trigger trigger : triggers)
        {
            // Check the trigger's class to see if it is a build trigger, and
            // the data map to see if the spec matches.
            Class clazz = trigger.getTaskClass();
            if (clazz.equals(BuildProjectTask.class))
            {
                long triggerSpecId = (Long) trigger.getDataMap().get(BuildProjectTask.PARAM_SPEC);

                if (triggerSpecId == specId && trigger.isScheduled())
                {
                    try
                    {
                        scheduler.unschedule(trigger);
                    }
                    catch (SchedulingException e)
                    {
                        throw new PulseRuntimeException("Unable to unschedule trigger [" + trigger.getId() + "]");
                    }
                }
            }
        }

        List<PostBuildAction> actions = project.getPostBuildActions();
        List<PostBuildAction> dead = new LinkedList<PostBuildAction>();
        for(PostBuildAction action: actions)
        {
            List<BuildSpecification> actionSpecs = action.getSpecifications();
            if(actionSpecs != null && actionSpecs.contains(spec))
            {
                if(actionSpecs.size() == 1)
                {
                    // The only spec: we should remove this action.
                    dead.add(action);
                }

                actionSpecs.remove(spec);
            }
        }

        for(PostBuildAction action: dead)
        {
            project.removePostBuildAction(action.getId());
        }

        project.remove(spec);
        buildSpecificationDao.delete(spec);
    }
*/

    public void buildCommenced(long projectId)
    {
        Project project = getProject(projectId, true);
        if (project != null)
        {
            project.buildCommenced();
            projectDao.save(project);
        }
    }

    public void buildCompleted(long projectId, boolean successful)
    {
        Project project = getProject(projectId, true);
        if (project != null)
        {
            project.buildCompleted();

            project.setBuildCount(project.getBuildCount() + 1);
            if(successful)
            {
                project.setSuccessCount(project.getSuccessCount() + 1);
            }

            projectDao.save(project);
        }
    }

    public Project pauseProject(Project project)
    {
        project = getProject(project.getId(), true);
        if (project != null)
        {
            project.pause();
            projectDao.save(project);
        }

        return project;
    }

    public void resumeProject(Project project)
    {
        project = getProject(project.getId(), true);
        if (project != null)
        {
            project.resume();
            projectDao.save(project);
        }
    }

    public void setProjectDao(ProjectDao dao)
    {
        projectDao = dao;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public Collection<ProjectGroup> getAllProjectGroups()
    {
        final Comparator<String> cmp = new Sort.StringComparator();
        Set<ProjectGroup> groups = new TreeSet<ProjectGroup>(new Comparator<ProjectGroup>()
        {
            public int compare(ProjectGroup o1, ProjectGroup o2)
            {
                return cmp.compare(o1.getName(), o2.getName());
            }
        });

        for(Map.Entry<String, Set<ProjectConfiguration>> entry: labelToConfigs.entrySet())
        {
            ProjectGroup group = createProjectGroup(entry.getKey(), entry.getValue());
            if(group.getProjects().size() > 0)
            {
                groups.add(group);
            }
        }
        
        return groups;
    }

    public ProjectGroup getProjectGroup(String name)
    {
        Set<ProjectConfiguration> projects = labelToConfigs.get(name);
        if(projects == null)
        {
            // Return an empty group.
            return new ProjectGroup(name);
        }
        else
        {
            return createProjectGroup(name, projects);
        }
    }

    private ProjectGroup createProjectGroup(String name, Set<ProjectConfiguration> projectConfigs)
    {
        ProjectGroup group = new ProjectGroup(name);
        Actor actor = accessManager.getActor();
        for(ProjectConfiguration config: projectConfigs)
        {
            if(accessManager.hasPermission(actor, AccessManager.ACTION_VIEW, config))
            {
                group.add(projectDao.findById(config.getProjectId()));
            }
        }

        return group;
    }

    public List<Project> mapConfigsToProjects(Collection<ProjectConfiguration> projects)
    {
        List<Project> result = new LinkedList<Project>();
        for(ProjectConfiguration config: projects)
        {
            Project project = projectDao.findById(config.getProjectId());
            if(project != null)
            {
                result.add(project);
            }
        }

        return result;
    }

    public void removeReferencesToAgent(long agentStateId)
    {
        for(Project p: getProjects(true))
        {
            if(p.clearForceCleanForAgent(agentStateId))
            {
                projectDao.save(p);
            }
        }
    }

    public void markForCleanBuild(Project project)
    {
        boolean changed = false;
        for(AgentState agentState: agentStateDao.findAll())
        {
            if(project.setForceCleanForAgent(agentState))
            {
                changed = true;
            }
        }

        if(changed)
        {
            projectDao.save(project);
        }
    }

    public void setConfiguration(Project state)
    {
        state.setConfig(idToConfig.get(state.getId()));
    }

    public void handleEvent(Event evt)
    {
        RecipeDispatchedEvent rde = (RecipeDispatchedEvent) evt;
        RecipeRequest request = rde.getRequest();
        ProjectConfiguration projectConfig = nameToConfig.get(request.getProject());
        if(projectConfig != null)
        {
            Project project = projectDao.findById(projectConfig.getProjectId());
            if(project != null)
            {
                if(project.clearForceCleanForAgent(rde.getAgent().getId()))
                {
                    projectDao.save(project);
                }
            }
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { RecipeDispatchedEvent.class };
    }

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
        this.eventManager = eventManager;
    }

    public void setTestCaseIndexDao(TestCaseIndexDao testCaseIndexDao)
    {
        this.testCaseIndexDao = testCaseIndexDao;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationInjector(ConfigurationInjector configurationInjector)
    {
        configurationInjector.registerSetter(Project.class, this);
    }

    public void setScmClientManager(DelegateScmClientFactory scmClientManager)
    {
        this.scmClientManager = scmClientManager;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    public void setAgentStateDao(AgentStateDao agentStateDao)
    {
        this.agentStateDao = agentStateDao;
    }
}
