package com.zutubi.pulse.model;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.license.authorisation.AddProjectAuthorisation;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
import com.zutubi.pulse.model.persistence.CommitMessageTransformerDao;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.model.persistence.TriggerDao;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;
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
    private static final Logger LOG = Logger.getLogger(DefaultProjectManager.class);

    private ProjectDao projectDao;
    private BuildSpecificationDao buildSpecificationDao;
    private TriggerDao triggerDao;
    private CommitMessageTransformerDao commitMessageTransformerDao;
    private Scheduler scheduler;
    private BuildManager buildManager;
    private SubscriptionManager subscriptionManager;
    private EventManager eventManager;
    private ChangelistIsolator changelistIsolator;

    private LicenseManager licenseManager;

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

    private void deleteProject(Project entity)
    {
        buildManager.deleteAllBuilds(entity);
        subscriptionManager.deleteAllSubscriptions(entity);
        projectDao.delete(entity);
    }

    public void delete(Project project)
    {
        project = getProject(project.getId());
        if (project != null)
        {
            deleteProject(project);
        }
    }

    @Secured({"ACL_PROJECT_WRITE"})
    public void checkWrite(Project project)
    {
//        boolean accessAllowed = false;
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(authentication != null)
//        {
//            AclEntry[] acls = aclManager.getAcls(project, authentication);
//            for(AclEntry acl: acls)
//            {
//                if(acl instanceof BasicAclEntry)
//                {
//                    BasicAclEntry basic = (BasicAclEntry) acl;
//                    if(basic.isPermitted(SimpleAclEntry.WRITE))
//                    {
//                        accessAllowed = true;
//                        break;
//                    }
//                }
//            }
//        }
//
//        if(!accessAllowed)
//        {
//            throw new AccessDeniedException("Access denied");
//        }
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

    public void triggerBuild(Project project, String specification, BuildReason reason, BuildRevision revision, boolean force)
    {
        BuildSpecification spec = project.getBuildSpecification(specification);
        if(spec == null)
        {
            LOG.warning("Request to build unknown specification '" + specification + "' of project '" + project.getName() + "'");
            return;
        }

        if(!revision.isFixed() && spec.getIsolateChangelists())
        {
            // In this case we need to check if there are multiple
            // outstanding revisions and if so create requests for each one.
            try
            {
                List<Revision> revisions = changelistIsolator.getRevisionsToRequest(project, spec, force);
                for(Revision r: revisions)
                {
                    requestBuildOfRevision(reason, project, specification, r);
                }
            }
            catch (SCMException e)
            {
                LOG.error("Unable to determine revisions to build for project '" + project.getName() + "', specification '" + specification + "': " + e.getMessage(), e);
            }
        }
        else
        {
            // Just raise one request.
            eventManager.publish(new BuildRequestEvent(this, reason, project, specification, revision));
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

    private void requestBuildOfRevision(BuildReason reason, Project project, String specification, Revision revision)
    {
        try
        {
            PulseFileDetails pulseFileDetails = project.getPulseFileDetails();
            ComponentContext.autowire(pulseFileDetails);
            String pulseFile = pulseFileDetails.getPulseFile(0, project, revision);
            eventManager.publish(new BuildRequestEvent(this, reason, project, specification, new BuildRevision(revision, pulseFile)));
        }
        catch (BuildException e)
        {
            LOG.severe("Unable to obtain pulse file for project '" + project.getName() + "', revision " + revision.getRevisionString() + ": " + e.getMessage(), e);
        }
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
        BuildSpecification spec = buildSpecificationDao.findById(specId);

        if (spec == null)
        {
            throw new PulseRuntimeException("Unknown build specification [" + specId + "]");
        }

        List<Trigger> triggers = triggerDao.findByProject(project.getId());
        for (Trigger trigger : triggers)
        {
            // Check the trigger's class to see if it is a build trigger, and
            // the data map to see if the spec matches.
            Class clazz = trigger.getTaskClass();
            if (clazz.equals(BuildProjectTask.class))
            {
                String specName = (String) trigger.getDataMap().get(BuildProjectTask.PARAM_SPEC);

                if (specName.equals(spec.getName()) && trigger.isScheduled())
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

    public void save(CommitMessageTransformer transformer)
    {
        commitMessageTransformerDao.save(transformer);
    }

    public CommitMessageTransformer getCommitMessageTransformer(long id)
    {
        return commitMessageTransformerDao.findById(id);
    }

    public void delete(CommitMessageTransformer transformer)
    {
        commitMessageTransformerDao.delete(transformer);
    }

    public List<CommitMessageTransformer> getCommitMessageTransformers()
    {
        return commitMessageTransformerDao.findAll();
    }

    public List<CommitMessageTransformer> findCommitMessageTransformersByProject(Project project)
    {
        return commitMessageTransformerDao.findByProject(project);
    }

    public CommitMessageTransformer findCommitMessageTransformerByName(String name)
    {
        return commitMessageTransformerDao.findByName(name);
    }

    public void setCommitMessageTransformerDao(CommitMessageTransformerDao commitMessageTransformerDao)
    {
        this.commitMessageTransformerDao = commitMessageTransformerDao;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }
}
