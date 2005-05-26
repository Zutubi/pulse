package com.cinnamonbob.core2;

import com.cinnamonbob.core2.schedule.Trigger;

import java.util.LinkedList;
import java.util.List;

/**
 * A schedule defines a set of criteria, that, when satisfied, will 
 * trigger the a build of the project. 
 * 
 * A schedule is associated with the recipe that will be executed. (optional)
 * 
 * These triggers are known as Schedulers.
 *
 */
public class Schedule
{
    /**
     * The name of the recipe this schedule triggers.
     */ 
    private String recipe;    
    
    private Project project;
    
    private List<Trigger> triggers = new LinkedList<Trigger>();

    /**
     * The project recipe triggered by this schedule. This recipe name should
     * refer to a configured recipe within the project.
     * 
     * @param recipe
     */ 
    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }

    /**
     * Add a new Scheduler (schedule trigger) to this schedule.
     * 
     * @param s
     */ 
    public void addTrigger(Trigger s)
    {
        triggers.add(s);
    }
    
    /**
     * Schedule the project for building.
     */ 
    public void schedule()
    {
        // loop through configured schedules and activate them.
        for (Trigger trigger : triggers)
        {
            trigger.activate();
        }
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }
}
