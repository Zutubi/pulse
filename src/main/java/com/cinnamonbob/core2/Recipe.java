package com.cinnamonbob.core2;

import com.cinnamonbob.core2.task.Task;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collections;

/**
 * 
 *
 */
public class Recipe implements Iterable<Task>
{
    /**
     * Recipe name. This name should be unique within a project.
     */
    private String name;

    /**
     * The project to which this recipe belongs.
     */
    private Project project;

    /**
     * The list of tasks that comprise this recipe. It is these
     * tasks that will be executed to 'run the build'.
     */
    private final List<Task> tasks = new LinkedList<Task>();

    /**
     * Setter for the project property.
     *
     * @param p
     * @see Recipe#project
     */
    public void setProject(Project p)
    {
        project = p;
    }

    /**
     * Getter for the project property
     *
     * @return
     * @see Recipe#project
     */
    public Project getProject()
    {
        return this.project;
    }

    /**
     * Getter for the name property.
     *
     * @return
     * @see Recipe#name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Setter for the name property
     *
     * @param name
     * @see Recipe#name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Execute the tasks associated with this recipe. 
     */ 
    public void execute()
    {        
        for (Task task: getTasks())
        {
            task.execute();
        }
    }

    /**
     * The tasks associated with this recipe.
     * @return
     * @see Recipe#tasks
     */ 
    public List<Task> getTasks()
    {
        return tasks;
    }

    /**
     * Add a task to this recipe. NOTE, tasks will be executed in the order
     * in which they are added.
     * @param task
     */ 
    public void addTask(Task task)
    {
        tasks.add(task);
    }

    /**
     * Get the number of tasks currently registered with this recipe.
     * 
     * @return
     */ 
    public int getTaskCount()
    {
        return tasks.size();
    }

    /**
     * Add a task at the specified index.
     * 
     * @param index
     * @param task
     */ 
    public void addTask(int index, Task task)
    {
        tasks.add(index, task);
    }

    public Iterator<Task> iterator()
    {
        // ensure that all modification of the list occurs via the public 
        // recipe interface.
        return Collections.unmodifiableList(tasks).iterator();
    }
}
