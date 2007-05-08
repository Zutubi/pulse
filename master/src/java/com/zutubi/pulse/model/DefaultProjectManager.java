package com.zutubi.pulse.model;

import com.zutubi.prototype.config.CollectionListener;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.cache.ehcache.CustomAclEntryCache;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.model.TestCaseIndex;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.events.build.PersonalBuildRequestEvent;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.license.authorisation.AddProjectAuthorisation;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.ProjectGroupDao;
import com.zutubi.pulse.model.persistence.TestCaseIndexDao;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.annotation.Secured;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 */
public class DefaultProjectManager implements ProjectManager
{
    public static final int DEFAULT_WORK_DIR_BUILDS = 10;

    private static final Logger LOG = Logger.getLogger(DefaultProjectManager.class);

    private ProjectDao projectDao;
    private ProjectGroupDao projectGroupDao;
    private TestCaseIndexDao testCaseIndexDao;
    private Scheduler scheduler;
    private BuildManager buildManager;
    private SubscriptionManager subscriptionManager;
    private EventManager eventManager;
    private ChangelistIsolator changelistIsolator;

    private LicenseManager licenseManager;
    private UserManager userManager;
    private CustomAclEntryCache projectAclEntryCache;

    private ConfigurationProvider configurationProvider;

    private Map<String, ProjectConfiguration> nameToConfig = new HashMap<String, ProjectConfiguration>();
    private Map<Long, ProjectConfiguration> idToConfig = new HashMap<Long, ProjectConfiguration>();

    public void initialise()
    {
        changelistIsolator = new ChangelistIsolator(buildManager);

        // register the canAddProject authorisation with the license manager.
        AddProjectAuthorisation addProjectAuthorisation = new AddProjectAuthorisation();
        addProjectAuthorisation.setProjectManager(this);
        licenseManager.addAuthorisation(addProjectAuthorisation);

        CollectionListener<ProjectConfiguration> listener = new CollectionListener<ProjectConfiguration>("project", ProjectConfiguration.class, true)
        {
            protected void preInsert(MutableRecord record)
            {
                Project project = new Project();
                save(project);
                record.put("projectId", Long.toString(project.getId()));
            }

            protected void instanceInserted(ProjectConfiguration instance)
            {
                registerProjectConfig(instance);
            }

            protected void instanceDeleted(ProjectConfiguration instance)
            {
                nameToConfig.remove(instance.getName());
                idToConfig.remove(instance.getProjectId());
            }

            protected void instanceChanged(ProjectConfiguration instance)
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
        configurationProvider.save("project", config.getName(), config);
    }

    public Project getProject(String name)
    {
        return projectDao.findByName(name);
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
        subscriptionManager.deleteAllSubscriptions(entity);
        userManager.removeReferencesToProject(entity);

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

    public void triggerBuild(Project project, BuildReason reason, Revision revision, boolean force)
    {
        ProjectConfiguration projectConfig = getProjectConfig(project.getId());
        if(projectConfig == null)
        {
            // Unlikely, but it may have been deleted
            return;
        }
        
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
                        requestBuildOfRevision(reason, projectConfig, project, r);
                    }
                }
                catch (ScmException e)
                {
                    LOG.error("Unable to determine revisions to build for project '" + projectConfig.getName() + "': " + e.getMessage(), e);
                }
            }
            else
            {
                eventManager.publish(new BuildRequestEvent(this, reason, projectConfig, project, new BuildRevision()));
            }
        }
        else
        {
            // Just raise one request.
            requestBuildOfRevision(reason, projectConfig, project, revision);
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
            String pulseFile = getPulseFile(projectConfig, project, revision, archive);
            eventManager.publish(new PersonalBuildRequestEvent(this, number, new BuildRevision(revision, pulseFile, false), user, archive, projectConfig, project));
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

    private void requestBuildOfRevision(BuildReason reason, ProjectConfiguration projectConfig, Project project, Revision revision)
    {
        try
        {
            String pulseFile = getPulseFile(projectConfig, project, revision, null);
            eventManager.publish(new BuildRequestEvent(this, reason, projectConfig, project, new BuildRevision(revision, pulseFile, reason.isUser())));
        }
        catch (BuildException e)
        {
            LOG.severe("Unable to obtain pulse file for project '" + projectConfig.getName() + "', revision '" + revision.getRevisionString() + "': " + e.getMessage(), e);
        }
    }

    private String getPulseFile(ProjectConfiguration projectConfig, Project project, Revision revision, PatchArchive patch) throws BuildException
    {
        PulseFileDetails pulseFileDetails = project.getPulseFileDetails();
        ComponentContext.autowire(pulseFileDetails);
        return pulseFileDetails.getPulseFile(0, projectConfig, project, revision, patch);
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

    @Secured({"ACL_PROJECT_WRITE"})
    public void deleteArtifact(Project project, long id)
    {
        project = getProject(project.getId());
        if(project != null && project.getPulseFileDetails().isBuiltIn())
        {
            TemplatePulseFileDetails details = (TemplatePulseFileDetails) project.getPulseFileDetails();
            Capture deadMan = null;

            for(Capture c: details.getCaptures())
            {
                if(c.getId() == id)
                {
                    deadMan = c;
                    break;
                }
            }

            if(deadMan != null)
            {
                details.removeCapture(deadMan);
            }
        }
    }

    public void buildCommenced(long projectId)
    {
        Project project = getProject(projectId);
        if (project != null)
        {
            project.buildCommenced();
            projectDao.save(project);
        }
    }

    public void buildCompleted(long projectId)
    {
        Project project = getProject(projectId);
        if (project != null)
        {
            project.buildCompleted();
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

    public void create(Project project) throws LicenseException
    {
        LicenseHolder.ensureAuthorization(LicenseHolder.AUTH_ADD_PROJECT);

        for(Group group: userManager.getAdminAllProjectGroups())
        {
            project.addAdmin(group.getDefaultAuthority());
        }

        // setup the project defaults.

// Fixme: this functionality is not present with the new plugin configuration system.  There is no concept of a
//        default inclusion by an external plugin to the project.        
//        project.addCleanupRule(new CleanupRule(true, null, DEFAULT_WORK_DIR_BUILDS, CleanupRule.CleanupUnit.BUILDS));

        projectDao.save(project);

        // create a simple build specification that executes the default recipe.
/*
        BuildSpecificationNode parent = buildSpecificationNodeDao.findById(buildSpec.getRoot().getId());
        BuildStage stage = new BuildStage("default", new AnyCapableBuildHostRequirements(), null);
        BuildSpecificationNode node = new BuildSpecificationNode(stage);
        parent.addChild(node);
        buildSpecificationNodeDao.save(parent);
*/

        // schedule the event trigger - unique to this project.
/*
        FIXME: need to convert triggers to config system.
        try
        {
            EventTrigger trigger = new EventTrigger(ScmChangeEvent.class, "scm trigger", project.getName(), ScmChangeEventFilter.class);
            trigger.setProject(project.getId());
            trigger.setTaskClass(BuildProjectTask.class);

            scheduler.schedule(trigger);
        }
        catch (SchedulingException e)
        {
            e.printStackTrace();
        }
*/

        projectDao.save(project);

        licenseManager.refreshAuthorisations();
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

    public void setSubscriptionManager(SubscriptionManager subscriptionManager)
    {
        this.subscriptionManager = subscriptionManager;
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
        userManager.removeReferencesToProjectGroup(projectGroup);
        projectGroupDao.delete(projectGroup);
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
}
