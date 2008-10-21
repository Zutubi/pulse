package com.zutubi.pulse.master.model;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.config.NamedConfigurationComparator;
import com.zutubi.pulse.core.model.TestCaseIndex;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmCapability;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.master.cleanup.config.CleanupUnit;
import com.zutubi.pulse.master.cleanup.config.CleanupWhat;
import com.zutubi.pulse.master.events.build.*;
import com.zutubi.pulse.master.events.system.ConfigurationEventSystemStartedEvent;
import com.zutubi.pulse.master.events.system.ConfigurationSystemStartedEvent;
import com.zutubi.pulse.master.license.LicenseManager;
import com.zutubi.pulse.master.license.authorisation.AddProjectAuthorisation;
import com.zutubi.pulse.master.model.persistence.AgentStateDao;
import com.zutubi.pulse.master.model.persistence.ProjectDao;
import com.zutubi.pulse.master.model.persistence.TestCaseIndexDao;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.SchedulingException;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.group.AbstractGroupConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectAclConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import com.zutubi.tove.config.*;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.security.Actor;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;
import com.zutubi.util.logging.Logger;

import java.util.*;

/**
 * 
 *
 */
public class DefaultProjectManager implements ProjectManager, ExternalStateManager<ProjectConfiguration>, ConfigurationInjector.ConfigurationSetter<Project>, EventListener
{
    private static final Logger LOG = Logger.getLogger(DefaultProjectManager.class);

    public static final int DEFAULT_WORK_DIR_BUILDS = 10;

    private ProjectDao projectDao;
    private TestCaseIndexDao testCaseIndexDao;
    private Scheduler scheduler;
    private BuildManager buildManager;
    private EventManager eventManager;
    private ChangelistIsolator changelistIsolator;
    private ScmManager scmManager;
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

    private void registerConfigListener(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
        TypeListener<ProjectConfiguration> listener = new TypeAdapter<ProjectConfiguration>(ProjectConfiguration.class)
        {
            public void postInsert(ProjectConfiguration instance)
            {
                registerProjectConfig(instance);
            }

            public void postDelete(ProjectConfiguration instance)
            {
                nameToConfig.remove(instance.getName());
                idToConfig.remove(instance.getProjectId());
                validConfigs.remove(instance);
                removeFromLabelMap(instance);
            }

            public void postSave(ProjectConfiguration instance, boolean nested)
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
        listener.register(configurationProvider, true);
    }

    private void initialise()
    {
        changelistIsolator = new ChangelistIsolator(buildManager);
        changelistIsolator.setScmManager(scmManager);

        // register the canAddProject authorisation with the license manager.
        AddProjectAuthorisation addProjectAuthorisation = new AddProjectAuthorisation();
        addProjectAuthorisation.setProjectManager(this);
        licenseManager.addAuthorisation(addProjectAuthorisation);

        updateProjects();

        // create default project if it is required.
        ensureDefaultProjectDefined();
    }

    /**
     * Ensure that the global template project is defined.
     */
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

                // Default cleanup rule to blow away working copy snapshots
                CleanupConfiguration cleanupConfiguration = new CleanupConfiguration();
                cleanupConfiguration.setName("default");
                cleanupConfiguration.setWhat(CleanupWhat.WORKING_DIRECTORIES_ONLY);
                cleanupConfiguration.setRetain(10);
                cleanupConfiguration.setUnit(CleanupUnit.BUILDS);
                Map<String, CleanupConfiguration> cleanupMap = new HashMap<String, CleanupConfiguration>();
                cleanupMap.put("default", cleanupConfiguration);
                globalProject.getExtensions().put("cleanup", cleanupMap);               

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

    public long createState(ProjectConfiguration instance)
    {
        Project project = new Project();
        save(project);

        return project.getId();
    }

    public void rollbackState(long id)
    {
        Project project = projectDao.findById(id);
        if (project != null)
        {
            projectDao.delete(project);
        }
    }

    public Object getState(long id)
    {
        return projectDao.findById(id);
    }

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
        if (allowInvalid || project != null && configurationTemplateManager.isDeeplyValid(project.getConfig().getConfigurationPath()))
        {
            return project;
        }

        return null;
    }

    public List<Project> getProjects(boolean allowInvalid)
    {
        List<Project> result = projectDao.findAll();
        if (!allowInvalid)
        {
            result = filterValidProjects(result);
        }

        return result;
    }

    public boolean isProjectValid(Project project)
    {
        return project.getConfig() != null && configurationTemplateManager.isDeeplyValid(project.getConfig().getConfigurationPath());
    }

    private List<Project> filterValidProjects(List<Project> projects)
    {
        return CollectionUtils.filter(projects, new Predicate<Project>()
        {
            public boolean satisfied(Project project)
            {
                return isProjectValid(project);
            }
        });
    }

    public int getProjectCount()
    {
        return getProjects(true).size();
    }

    public void abortUnfinishedBuilds(Project project, String message)
    {
        List<BuildResult> abortedBuilds = buildManager.abortUnfinishedBuilds(project, message);
        if (abortedBuilds.size() > 0)
        {
            project.setBuildCount(project.getBuildCount() + abortedBuilds.size());
            projectDao.save(project);
        }
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

    public void triggerBuild(ProjectConfiguration projectConfig, BuildReason reason, Revision revision, String source, boolean replaceable, boolean force)
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
                    Set<ScmCapability> capabilities = ScmClientUtils.getCapabilities(projectConfig.getScm(), scmManager);
                    if(capabilities.contains(ScmCapability.REVISIONS))
                    {
                        if (scmManager.isReady(projectConfig.getScm()))
                        {
                            List<Revision> revisions = changelistIsolator.getRevisionsToRequest(projectConfig, project, force);
                            for(Revision r: revisions)
                            {
                                // Note when isolating changelists we never replace existing requests
                                requestBuildOfRevision(reason, project, r, source, false);
                            }
                        }
                        else
                        {
                            LOG.warning("Unable to use changelist isolation for project '" + projectConfig.getName() +
                                    "' as the SCM does not support revisions");
                            requestBuildFloating(reason, project, source, replaceable);
                        }
                    }
                    else
                    {
                        LOG.warning("Unable to use changelist isolation for project '" + projectConfig.getName() +
                                "' as the SCM is not ready to calculate the changelists");
                        requestBuildFloating(reason, project, source, replaceable);
                    }
                }
                catch (ScmException e)
                {
                    LOG.error("Unable to determine revisions to build for project '" + projectConfig.getName() + "': " + e.getMessage(), e);
                }
            }
            else
            {
                requestBuildFloating(reason, project, source, replaceable);
            }
        }
        else
        {
            // Just raise one request.
            requestBuildOfRevision(reason, project, revision, source, replaceable);
        }
    }

    public void triggerBuild(long number, Project project, User user, PatchArchive archive) throws PulseException
    {
        ProjectConfiguration projectConfig = getProjectConfig(project.getId(), false);
        if(projectConfig == null)
        {
            return;
        }

        Revision revision = archive.getMetadata().getRevision();
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

    private void requestBuildFloating(BuildReason reason, Project project, String category, boolean replaceable)
    {
        eventManager.publish(new BuildRequestEvent(this, reason, project, new BuildRevision(), category, replaceable));
    }

    private void requestBuildOfRevision(BuildReason reason, Project project, Revision revision, String source, boolean replaceable)
    {
        try
        {
            String pulseFile = getPulseFile(project.getConfig(), revision, null);
            eventManager.publish(new BuildRequestEvent(this, reason, project, new BuildRevision(revision, pulseFile, reason.isUser()), source, replaceable));
        }
        catch (Exception e)
        {
            LOG.severe("Unable to obtain pulse file for project '" + project.getName() + "', revision '" + revision.getRevisionString() + "': " + e.getMessage(), e);
        }
    }

    private String getPulseFile(ProjectConfiguration projectConfig, Revision revision, PatchArchive patch) throws Exception
    {
        TypeConfiguration type = projectConfig.getType();
        return type.getPulseFile(projectConfig, revision, patch);
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
        long projectId = state.getId();
        ProjectConfiguration projectConfiguration = idToConfig.get(projectId);
        state.setConfig(projectConfiguration);
    }

    private void handleBuildActivated(BuildActivatedEvent event)
    {
        AbstractBuildRequestEvent buildRequestEvent = event.getEvent();
        if (!buildRequestEvent.isPersonal())
        {
            Project project = getProject(buildRequestEvent.getOwner().getId(), true);
            if (project != null)
            {
                project.buildCommenced();
                projectDao.save(project);
            }
        }
    }

    private void handleBuildCompleted(BuildCompletedEvent event)
    {
        BuildResult buildResult = event.getBuildResult();
        if (!buildResult.isPersonal())
        {
            Project project = getProject(buildResult.getProject().getId(), true);
            if (project != null)
            {
                project.buildCompleted();
                project.setBuildCount(project.getBuildCount() + 1);
                if (buildResult.succeeded())
                {
                    project.setSuccessCount(project.getSuccessCount() + 1);
                }

                projectDao.save(project);
            }
        }
    }

    private void handleRecipeAssigned(RecipeAssignedEvent rde)
    {
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

    public void handleEvent(Event evt)
    {
        if (evt instanceof BuildActivatedEvent)
        {
            handleBuildActivated((BuildActivatedEvent) evt);
        }
        else if (evt instanceof BuildCompletedEvent)
        {
            handleBuildCompleted((BuildCompletedEvent) evt);
        }
        else if (evt instanceof RecipeAssignedEvent)
        {
            handleRecipeAssigned((RecipeAssignedEvent) evt);
        }
        else if(evt instanceof ConfigurationEventSystemStartedEvent)
        {
            ConfigurationEventSystemStartedEvent cesse = (ConfigurationEventSystemStartedEvent) evt;
            registerConfigListener(cesse.getConfigurationProvider());
        }
        else
        {
            initialise();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { BuildActivatedEvent.class,
                             BuildCompletedEvent.class,
                             RecipeAssignedEvent.class,
                             ConfigurationEventSystemStartedEvent.class,
                             ConfigurationSystemStartedEvent.class };
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

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setConfigurationStateManager(ConfigurationStateManager configurationStateManager)
    {
        configurationStateManager.register(ProjectConfiguration.class, this);
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationInjector(ConfigurationInjector configurationInjector)
    {
        configurationInjector.registerSetter(Project.class, this);
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
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
