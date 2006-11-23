package com.zutubi.pulse.model;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.events.build.PersonalBuildRequestEvent;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.license.authorisation.AddProjectAuthorisation;
import com.zutubi.pulse.model.persistence.*;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.scheduling.*;
import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.scm.SCMChangeEvent;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.util.logging.Logger;
import org.acegisecurity.annotation.Secured;

import java.util.List;

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
    private BuildSpecificationDao buildSpecificationDao;
    private BuildSpecificationNodeDao buildSpecificationNodeDao;
    private TriggerDao triggerDao;
    private Scheduler scheduler;
    private BuildManager buildManager;
    private SubscriptionManager subscriptionManager;
    private EventManager eventManager;
    private ChangelistIsolator changelistIsolator;

    private LicenseManager licenseManager;
    private UserManager userManager;

    public void save(Project project)
    {
        projectDao.save(project);
    }

    public Project getProject(String name)
    {
        return projectDao.findByName(name);
    }

    public Project getProject(long id)
    {
        return projectDao.findById(id);
    }

    public Project getProjectByScm(long scmId)
    {
        return projectDao.findByScmId(scmId);
    }

    public Project getProjectByBuildSpecification(BuildSpecification buildSpecification)
    {
        return projectDao.findByBuildSpecification(buildSpecification);
    }

    public List<Project> getAllProjects()
    {
        return projectDao.findAll();
    }

    public List<Project> getProjectsWithNameLike(String name)
    {
        return projectDao.findByLikeName(name);
    }

    public int getProjectCount()
    {
        return projectDao.count();
    }

    public void save(BuildSpecification specification)
    {
        buildSpecificationDao.save(specification);
    }

    public void setDefaultBuildSpecification(Project project, long specId)
    {
        BuildSpecification spec = project.getBuildSpecification(specId);
        if(spec == null)
        {
            throw new IllegalArgumentException("Unknown build specificaiton [" + specId + "]");
        }

        project.setDefaultSpecification(spec);
    }

    private void deleteProject(Project entity)
    {
        try
        {
            scheduler.unscheduleAllTriggers(entity.getId());
        }
        catch (SchedulingException e)
        {
            LOG.warning("Unable to unschedule triggers for project '" + entity.getName() + "'", e);
        }
        
        buildManager.deleteAllBuilds(entity);
        subscriptionManager.deleteAllSubscriptions(entity);
        userManager.removeReferencesToProject(entity);
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

    public Project cloneProject(Project project, String name, String description)
    {
        Project copy = project.copy(name, description);
        projectDao.save(copy);

        List<Trigger> triggers = scheduler.getTriggers(project.getId());
        for(Trigger t: triggers)
        {
            Trigger triggerCopy = t.copy(project, copy);
            try
            {
                scheduler.schedule(triggerCopy);
            }
            catch (SchedulingException e)
            {
                LOG.severe("Unable to schedule trigger: " + e);
            }
        }

        return copy;
    }

    public void triggerBuild(Project project, String specification, BuildReason reason, Revision revision, boolean force)
    {
        BuildSpecification spec = project.getBuildSpecification(specification);
        if(spec == null)
        {
            LOG.warning("Request to build unknown specification '" + specification + "' of project '" + project.getName() + "'");
            return;
        }

        if(revision == null)
        {
            if(spec.getIsolateChangelists())
            {
                // In this case we need to check if there are multiple
                // outstanding revisions and if so create requests for each one.
                try
                {
                    List<Revision> revisions = changelistIsolator.getRevisionsToRequest(project, spec, force);
                    for(Revision r: revisions)
                    {
                        requestBuildOfRevision(reason, project, spec, r);
                    }
                }
                catch (SCMException e)
                {
                    LOG.error("Unable to determine revisions to build for project '" + project.getName() + "', specification '" + specification + "': " + e.getMessage(), e);
                }
            }
            else
            {
                eventManager.publish(new BuildRequestEvent(this, reason, project, spec, new BuildRevision()));
            }
        }
        else
        {
            // Just raise one request.
            requestBuildOfRevision(reason, project, spec, revision);
        }
    }

    public void triggerBuild(long number, Project project, BuildSpecification specification, User user, PatchArchive archive) throws PulseException
    {
        Revision revision = archive.getStatus().getRevision();
        try
        {
            String pulseFile = getPulseFile(project, revision, archive);
            eventManager.publish(new PersonalBuildRequestEvent(this, number, new BuildRevision(revision, pulseFile), user, archive, project, specification));
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

    private void requestBuildOfRevision(BuildReason reason, Project project, BuildSpecification specification, Revision revision)
    {
        try
        {
            String pulseFile = getPulseFile(project, revision, null);
            eventManager.publish(new BuildRequestEvent(this, reason, project, specification, new BuildRevision(revision, pulseFile)));
        }
        catch (BuildException e)
        {
            String message = "Unable to obtain pulse file for project '" + project.getName();
            if(revision != null)
            {
                message += "', revision '" + revision.getRevisionString();
            }
            LOG.severe(message + "': " + e.getMessage(), e);
        }
    }

    private String getPulseFile(Project project, Revision revision, PatchArchive patch) throws BuildException
    {
        PulseFileDetails pulseFileDetails = project.getPulseFileDetails();
        ComponentContext.autowire(pulseFileDetails);
        return pulseFileDetails.getPulseFile(0, project, revision, patch);
    }

    public void updateProjectDetails(Project project, String name, String description, String url) throws SchedulingException
    {
        project = getProject(project.getId());
        if(!project.getName().equals(name))
        {
            // Name has changed, triggers must be updated
            scheduler.renameProjectTriggers(project.getId(), name);
        }

        project.setName(name);
        project.setDescription(description);
        project.setUrl(url);
        projectDao.save(project);
    }

    public List<Project> getProjectsWithAdmin(String authority)
    {
        return projectDao.findByAdminAuthority(authority);
    }

    public void updateProjectAdmins(String authority, List<Long> restrictToProjects)
    {
        List<Project> projects = getAllProjects();
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
        }
    }

    public void removeAcls(String authority)
    {
        List<Project> projects = projectDao.findByAdminAuthority(authority);
        for(Project p: projects)
        {
            p.removeAdmin(authority);
            save(p);
        }
    }

    public void initialise()
    {
        changelistIsolator = new ChangelistIsolator(buildManager);

        // register the canAddProject authorisation with the license manager.
        AddProjectAuthorisation addProjectAuthorisation = new AddProjectAuthorisation();
        addProjectAuthorisation.setProjectManager(this);
        licenseManager.addAuthorisation(addProjectAuthorisation);
    }

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

        project.remove(spec);
        buildSpecificationDao.delete(spec);
    }

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

        BuildSpecification buildSpec = new BuildSpecification("default");
        project.addBuildSpecification(buildSpec);
        project.setDefaultSpecification(buildSpec);
        project.addCleanupRule(new CleanupRule(true, null, DEFAULT_WORK_DIR_BUILDS, CleanupRule.CleanupUnit.BUILDS));

        projectDao.save(project);

        // create a simple build specification that executes the default recipe.
        BuildSpecificationNode parent = buildSpecificationNodeDao.findById(buildSpec.getRoot().getId());
        BuildStage stage = new BuildStage("default", new AnyCapableBuildHostRequirements(), null);
        BuildSpecificationNode node = new BuildSpecificationNode(stage);
        parent.addChild(node);
        buildSpecificationNodeDao.save(parent);

        // schedule the event trigger - unique to this project.
        try
        {
            EventTrigger trigger = new EventTrigger(SCMChangeEvent.class, "scm trigger", project.getName(), SCMChangeEventFilter.class);
            trigger.setProject(project.getId());
            trigger.setTaskClass(BuildProjectTask.class);
            trigger.getDataMap().put(BuildProjectTask.PARAM_SPEC, buildSpec.getId());

            scheduler.schedule(trigger);
        }
        catch (SchedulingException e)
        {
            e.printStackTrace();
        }

        projectDao.save(project);

        licenseManager.refreshAuthorisations();
    }

    public void setProjectDao(ProjectDao dao)
    {
        projectDao = dao;
    }

    /**
     * Required resource.
     *
     * @param buildSpecificationDao
     */
    public void setBuildSpecificationDao(BuildSpecificationDao buildSpecificationDao)
    {
        this.buildSpecificationDao = buildSpecificationDao;
    }

    /**
     * Required resource.
     *
     * @param triggerDao
     */
    public void setTriggerDao(TriggerDao triggerDao)
    {
        this.triggerDao = triggerDao;
    }

    /**
     * Required resource.
     *
     * @param scheduler
     */
    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    /**
     * Required resource.
     *
     * @param buildManager
     */
    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    /**
     * Required resource.
     * 
     * @param subscriptionManager
     */
    public void setSubscriptionManager(SubscriptionManager subscriptionManager)
    {
        this.subscriptionManager = subscriptionManager;
    }

    public List<ProjectGroup> getAllProjectGroups()
    {
        return projectGroupDao.findAll();
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

    public Project getProjectByCleanupRule(CleanupRule rule)
    {
        return projectDao.findByCleanupRule(rule);
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

    public void setBuildSpecificationNodeDao(BuildSpecificationNodeDao buildSpecificationNodeDao)
    {
        this.buildSpecificationNodeDao = buildSpecificationNodeDao;
    }
}
