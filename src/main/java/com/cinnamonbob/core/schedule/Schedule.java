package com.cinnamonbob.core.schedule;

import com.cinnamonbob.core.Project;

import java.util.List;
import java.util.LinkedList;

/**
 * Define a build schedule for a specific recipe.
 *
 */
public class Schedule
{
    private Project project;
    private String recipe;
    
    private List<Trigger> triggers = new LinkedList<Trigger>();
    
    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }
    
    public String getRecipe()
    {
        return this.recipe;
    }
    
    public void setProject(Project project)
    {
        this.project = project;
    }
    
    public Project getProject()
    {
        return this.project;
    }

    /**
     * Add a new trigger for this schedule.
     * 
     * @param trigger
     */ 
    public void addTrigger(Trigger trigger)
    {        
        trigger.setSchedule(this);
        triggers.add(trigger);
    }

    /**
     * 
     */ 
    public void triggered()
    {
        // generate a build request for this project -> recipe.
        // project.getName();
        // project.getRecipe(recipe);       
    }
    
    public void activate()
    {
        for (Trigger trigger: triggers)
        {
            if (!trigger.isEnabled())
            {
                trigger.enable();
            }
        }
    }
    
    public void deactivate()
    {
        for (Trigger trigger: triggers)
        {
            if (trigger.isEnabled())
            {
                trigger.disable();
            }
        }
        
    }
}
