package com.zutubi.pulse.model;

import com.zutubi.prototype.config.*;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.cache.ehcache.CustomAclEntryCache;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.model.TestCaseIndex;
import com.zutubi.pulse.core.scm.DelegateScmClientFactory;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.events.build.PersonalBuildRequestEvent;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.license.authorisation.AddProjectAuthorisation;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.ProjectGroupDao;
import com.zutubi.pulse.model.persistence.TestCaseIndexDao;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.project.types.TypeConfiguration;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.annotation.Secured;

import java.util.*;

/**
 * 
 *
 */
public class DefaultProjectManager implements ProjectManager, ConfigurationInjector.ConfigurationSetter<Project>
{
    private static final Logger LOG = Logger.getLogger(DefaultProjectManager.class);

    public static final int DEFAULT_WORK_DIR_BUILDS = 10;

    private static final String GLOBAL_PROJECT_NAME = "global project template";

    private ProjectDao projectDao;
    private ProjectGroupDao projectGroupDao;
    private TestCaseIndexDao testCaseIndexDao;
    private Scheduler scheduler;
    private BuildManager buildManager;
    private EventManager eventManager;
    private ChangelistIsolator changelistIsolator;
    private DelegateScmClientFactory scmClientManager;
    private LicenseManager licenseManager;
    private UserManager userManager;
    private CustomAclEntryCache projectAclEntryCache;

    private ConfigurationProvider configurationProvider;
    private TypeRegistry typeRegistry;
    private ConfigurationTemplateManager configurationTemplateManager;

    private Map<String, ProjectConfiguration> nameToConfig = new HashMap<String, ProjectConfiguration>();
    private Map<Long, ProjectConfiguration> idToConfig = new HashMap<Long, ProjectConfiguration>();

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

            public void preDelete(ProjectConfiguration instance)
            {
                nameToConfig.remove(instance.getName());
                idToConfig.remove(instance.getProjectId());
            }

            public void postSave(ProjectConfiguration instance)
            {
                // Tricky: the name may have changed.
                ProjectConfiguration old = idToConfig.remove(instance.getProjectId());
                if(old != null)
                {
                    nameToConfig.remove(old.getName());
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
            CompositeType projectType = typeRegistry.getType(ProjectConfiguration.class);
            MutableRecord globalTemplate = projectType.createNewRecord(true);
            globalTemplate.put("name", GLOBAL_PROJECT_NAME);
            globalTemplate.put("description", "The global template is the base of the project template hierarchy.  Configuration shared among all projects should be added here.");
            configurationTemplateManager.markAsTemplate(globalTemplate);
            configurationTemplateManager.insertRecord(ConfigurationRegistry.PROJECTS_SCOPE, globalTemplate);
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
    }

    public void save(Project project)
    {
        projectDao.save(project);
    }

    public Collection<ProjectConfiguration> getAllProjectConfigs()
    {
        return Collections.unmodifiableCollection(nameToConfig.values());
    }

    public ProjectConfiguration getProjectConfig(String name)
    {
        return nameToConfig.get(name);
    }

    public ProjectConfiguration getProjectConfig(long id)
    {
        return idToConfig.get(id);
    }

    public void saveProjectConfig(ProjectConfiguration config)
    {
        configurationProvider.save(config);
    }

    public Project getProject(String name)
    {
        ProjectConfiguration config = nameToConfig.get(name);
        if(config == null)
        {
            return null;
        }
        return projectDao.findById(config.getProjectId());
    }

    public Project getProject(long id)
    {
        return projectDao.findById(id);
    }

    public List<Project> getProjects()
    {
        return projectDao.findAll();
    }

    public List<Project> getAllProjectsCached()
    {
        return projectDao.findAllProjectsCached();
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

        // cleanup the associated project groups.
        for (ProjectGroup group: projectGroupDao.findByProject(entity))
        {
            group.remove(entity);
        }
        
        projectDao.delete(entity);
    }

    public void delete(Project project)
    {
        project = getProject(project.getId());
        if (project != null)
        {
            deleteProject(project);
        }

        licenseManager.refreshAuthorisations();
    }

    @Secured({"ACL_PROJECT_WRITE"})
    public void checkWrite(Project project)
    {
    }

    public void triggerBuild(ProjectConfiguration projectConfig, BuildReason reason, Revision revision, boolean force)
    {
        Project project = getProject(projectConfig.getProjectId());

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
        ProjectConfiguration projectConfig = getProjectConfig(project.getId());
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
        catch (BuildException e)
        {
            throw new PulseException(e.getMessage(), e);
        }
    }

    public long getNextBuildNumber(Project project)
    {
        project = getProject(project.getId());
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
        catch (BuildException e)
        {
            LOG.severe("Unable to obtain pulse file for project '" + project.getName() + "', revision '" + revision.getRevisionString() + "': " + e.getMessage(), e);
        }
    }

    private String getPulseFile(ProjectConfiguration projectConfig, Revision revision, PatchArchive patch) throws BuildException
    {
        TypeConfiguration type = projectConfig.getType();
        ComponentContext.autowire(type);
        return type.getPulseFile(0, projectConfig, revision, patch);
    }

    public List<Project> getProjectsWithAdmin(String authority)
    {
        return projectDao.findByAdminAuthority(authority);
    }

    public void updateProjectAdmins(String authority, List<Long> restrictToProjects)
    {
        List<Project> projects = getProjects();
        for(Project p: projects)
        {
            if(restrictToProjects == null || restrictToProjects.contains(p.getId()))
            {
                p.addAdmin(authority);
            }
            else
            {
                p.removeAdmin(authority);
            }

            save(p);
            projectAclEntryCache.removeEntriesFromCache(p);
        }
    }

    public void removeAcls(String authority)
    {
        List<Project> projects = projectDao.findByAdminAuthority(authority);
        for(Project p: projects)
        {
            p.removeAdmin(authority);
            save(p);
            projectAclEntryCache.removeEntriesFromCache(p);
        }
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
        Project project = getProject(projectId);
        if (project != null)
        {
            project.buildCommenced();
            projectDao.save(project);
        }
    }

    public void buildCompleted(long projectId, boolean successful)
    {
        Project project = getProject(projectId);
        if (project != null)
        {
            project.buildCompleted();

            if (project.isForceClean())
            {
                project.setForceClean(false);
            }

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
        project = getProject(project.getId());
        if (project != null)
        {
            project.pause();
            projectDao.save(project);
        }

        return project;
    }

    public void resumeProject(Project project)
    {
        project = getProject(project.getId());
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

    public List<ProjectGroup> getAllProjectGroups()
    {
        return projectGroupDao.findAll();
    }

    public List<ProjectGroup> getAllProjectGroupsCached()
    {
        return projectGroupDao.findAllCached();
    }

    public ProjectGroup getProjectGroup(long id)
    {
        return projectGroupDao.findById(id);
    }

    public ProjectGroup getProjectGroup(String name)
    {
        return projectGroupDao.findByName(name);
    }

    public void save(ProjectGroup projectGroup)
    {
        projectGroupDao.save(projectGroup);
    }

    @Secured({"ROLE_ADMINISTRATOR"})
    public void delete(ProjectGroup projectGroup)
    {
        projectGroupDao.delete(projectGroup);
    }

    public List<Project> mapConfigsToProjects(List<ProjectConfiguration> projects)
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

    public void setConfiguration(Project state)
    {
        state.setConfig(idToConfig.get(state.getId()));
    }

    public void delete(BuildHostRequirements hostRequirements)
    {
        projectDao.delete(hostRequirements);
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setProjectGroupDao(ProjectGroupDao projectGroupDao)
    {
        this.projectGroupDao = projectGroupDao;
    }

    public void setTestCaseIndexDao(TestCaseIndexDao testCaseIndexDao)
    {
        this.testCaseIndexDao = testCaseIndexDao;
    }

    public void setProjectAclEntryCache(CustomAclEntryCache projectAclEntryCache)
    {
        this.projectAclEntryCache = projectAclEntryCache;
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
}
