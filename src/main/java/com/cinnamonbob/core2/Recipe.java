package com.cinnamonbob.core2;

import com.cinnamonbob.core2.task.Task;

import java.util.List;
import java.util.LinkedList;

/**
 * 
 *
 */
public class Recipe
{
    private String name;
    
    private Project project;
    
    private List<Task> tasks = new LinkedList<Task>();

    public void setProject(Project p)
    {
        project = p;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Task> getTasks()
    {
        return tasks;
    }

    public void addTask(Task task)
    {
        tasks.add(task);
    }
}
