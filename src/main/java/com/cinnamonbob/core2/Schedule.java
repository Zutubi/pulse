package com.cinnamonbob.core2;

import com.cinnamonbob.core2.schedule.Scheduler;

import java.util.List;
import java.util.LinkedList;

/**
 * 
 *
 */
public class Schedule
{
    private String recipe;

    private List<Scheduler> schedules = new LinkedList<Scheduler>();
    
    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }
    
    public void addScheduler(Scheduler s)
    {
        schedules.add(s);
    }
    
}
