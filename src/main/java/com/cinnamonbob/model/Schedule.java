package com.cinnamonbob.model;

import java.util.List;
import java.util.LinkedList;

import com.cinnamonbob.BobServer;

/**
 * 
 *
 */
public class Schedule extends Entity
{
    private String name;
    private Project project;
    private String recipe;
    
    private List<Trigger> triggers = new LinkedList<Trigger>();

    public Schedule()
    {
        
    }
    
    public Schedule(String name, Project project, String recipe)
    {
        this.name = name;
        this.project = project;
        this.recipe = recipe;
    }
    
    public Project getProject()
    {
        return project;
    }
    
    public void setProject(Project project)
    {
        this.project = project;
    }

    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }
    
    public String getRecipe()
    {
        return this.recipe;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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
        BobServer.build(project.getName(), recipe);       
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
    
    public List<Trigger> getTriggers()
    {
        return triggers;
    }

    private void setTriggers(List<Trigger> triggers)
    {
        this.triggers = triggers;
    }

    public void remove(long id)
    {
        int i;
        
        for(i = 0; i < triggers.size(); i++)
        {
            if(triggers.get(i).getId() == id)
            {
                triggers.remove(i);
                return;
            }
        }
    }
}
