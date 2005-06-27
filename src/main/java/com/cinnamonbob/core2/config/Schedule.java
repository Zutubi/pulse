package com.cinnamonbob.core2.config;

import java.util.List;
import java.util.LinkedList;

/**
 * 
 *
 */
public class Schedule implements ProjectComponent
{
    private String recipe;
    private String name;
    private Project project;
    
    private List<Trigger> triggers = new LinkedList<Trigger>();

    public void setProject(Project project)
    {
        this.project = project;
    }

    public Project getProject()
    {
        return project;
    }
    
    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }
    
    public String getRecipe()
    {
        return this.recipe;
    }

    /**
     * Add a new trigger for this schedule.
     * 
     * @param trigger
     */ 
    public void add(Trigger trigger)
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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public List<Trigger> getTriggers()
    {
        return triggers;
    }

}
