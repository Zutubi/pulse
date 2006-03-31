package com.cinnamonbob.model;

import com.cinnamonbob.core.BobRuntimeException;
import com.cinnamonbob.model.persistence.BuildSpecificationDao;
import com.cinnamonbob.model.persistence.ProjectDao;
import com.cinnamonbob.scheduling.Scheduler;
import com.cinnamonbob.scheduling.SchedulingException;
import com.cinnamonbob.scheduling.Trigger;
import com.cinnamonbob.scheduling.persistence.TriggerDao;
import com.cinnamonbob.scheduling.tasks.BuildProjectTask;

import java.util.List;

/**
 * 
 *
 */
public class DefaultProjectManager implements ProjectManager
{
    private ProjectDao projectDao;
    private BuildSpecificationDao buildSpecificationDao;
    private TriggerDao triggerDao;
    private Scheduler scheduler;
    private BuildManager buildManager;
    private SubscriptionManager subscriptionManager;

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

    public List<Project> getAllProjects()
    {
        return projectDao.findAll();
    }

    public List<Project> getProjectsWithNameLike(String name)
    {
        return projectDao.findByLikeName(name);
    }

    public void delete(Project entity)
    {
        buildManager.deleteAllBuilds(entity);
        subscriptionManager.deleteAllSubscriptions(entity);
        projectDao.delete(entity);
    }

    public void delete(long projectId)
    {
        Project project = getProject(projectId);
        if (project != null)
        {
            delete(project);
        }
    }

    public void initialise()
    {
    }

    public void deleteBuildSpecification(long projectId, long specId)
    {
        BuildSpecification spec = buildSpecificationDao.findById(specId);

        if (spec == null)
        {
            throw new BobRuntimeException("Unknown build specification [" + specId + "]");
        }

        List<Trigger> triggers = triggerDao.findByProject(projectId);
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
                        throw new BobRuntimeException("Unable to unschedule trigger [" + trigger.getId() + "]");
                    }
                }
            }
        }

        buildSpecificationDao.delete(spec);
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

    public Project pauseProject(long projectId)
    {
        Project project = getProject(projectId);
        if (project != null)
        {
            project.pause();
            projectDao.save(project);
        }

        return project;
    }

    public void resumeProject(long projectId)
    {
        Project project = getProject(projectId);
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

    public void setBuildSpecificationDao(BuildSpecificationDao buildSpecificationDao)
    {
        this.buildSpecificationDao = buildSpecificationDao;
    }

    public void setTriggerDao(TriggerDao triggerDao)
    {
        this.triggerDao = triggerDao;
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
}
