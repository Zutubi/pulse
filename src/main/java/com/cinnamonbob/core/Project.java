package com.cinnamonbob.core;

import com.cinnamonbob.bootstrap.quartz.QuartzManager;
import com.cinnamonbob.core.config.Schedule;
import com.cinnamonbob.core.config.Reference;
import com.cinnamonbob.core.config.VariableHelper;
import com.cinnamonbob.BobException;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * A project describes a set of components and recipes to "build" those
 * components.
 */
public class Project
{
    //=======================================================================
    // Constants
    //=======================================================================

    private static final Logger LOG = Logger.getLogger(Project.class.getName());

    //=======================================================================
    // Types
    //=======================================================================

    public enum State
    {
        BUILDING,
        IDLE,
        PAUSED,
        PAUSING
    }
    
    //=======================================================================
    // Fields
    //=======================================================================

    /**
     * The name of this project, which must be unique amongst all projects.
     */
    private String name;
    
    /**
     * The project's current state.
     */
    private State state;
    
    /**
     * A human-readable description of this project.
     */
    private String description;
    
    /**
     * Registry of known feature categories.
     */
    public FeatureCategoryRegistry categoryRegistry;
    
    /**
     * Post-processors defined for this project.
     */
    public Map<String, PostProcessorCommon> postProcessors;

    /**
     * The recipes associated with this project.
     */ 
    private List<Recipe> recipes = new LinkedList<Recipe>();

    /**
     * The name of the default recipe, ie: the recipe that will be built if
     * no recipe is specified.
     */ 
    private String defaultRecipe = "";
    
    /**
     * Subscriptions to events on this project.
     */
    private List<Subscription> subscriptions;

    /**
     * The result of the currently running or last completed build (if any).
     */
    private BuildResult currentBuild;
    
    /**
     * The next available build id.
     */
    private int nextBuild;

    /**
     * The list of schedules configured for this project.
     */ 
    public List<Schedule> schedules = new LinkedList<Schedule>();
    
    private Map<String, String> properties = new HashMap<String, String>();
    private Map<String, Reference> references = new HashMap<String, Reference>();
    
    //=======================================================================
    // Construction
    //=======================================================================

    /**
     * No argument constructor required by xml loading process.
     */ 
    public Project()
    {
        this.postProcessors   = new TreeMap<String, PostProcessorCommon>();
        this.subscriptions    = new LinkedList<Subscription>();
        this.categoryRegistry = new FeatureCategoryRegistry();
        this.state            = State.IDLE;
    }
    
    //=======================================================================
    // Implementation
    //=======================================================================

    private synchronized boolean initiateBuild()
    {
        if(state != State.PAUSED && state != State.PAUSING)
        {
            state = State.BUILDING;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private synchronized void buildComplete()
    {
        if(state == State.PAUSING)
        {
            state = State.PAUSED;
        }
        else
        {
            state = State.IDLE;
        }
    }
    
    private BuildManager getBuildManager()
    {
        return BuildManager.getInstance();
    }    

    //=======================================================================
    // Interface
    //=======================================================================
    
    /**
     * Adds a subscription to events on this project.
     * 
     * @param subscription
     *        the subscription to add
     */
    public void addSubscription(Subscription subscription)
    {
        subscriptions.add(subscription);
    }

    /**
     * @return the name of this project.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name of this project.
     */ 
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @return the description of this project
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param desc of this project
     */ 
    public void setDescription(String desc)
    {
        this.description = desc;
    }
    
    /**
     * @return the current state of the project
     */
    public State getState()
    {
        return state;
    }
    
    public void setDefaultRecipe(String defaultRecipe)
    {
        this.defaultRecipe = defaultRecipe;
    }
    
    public String getDefaultRecipe()
    {
        return this.defaultRecipe;
    }
    
    /**
     * Executes a build of this project.
     * 
     */
    public void build()
    {
        if(initiateBuild())
        {
            currentBuild = new BuildResult(name, nextBuild, categoryRegistry);
            getBuildManager().executeBuild(this, currentBuild);
            
            // Don't increment nextBuild until we have finished the build, this
            // way the build won't be picked up by getHistory until complete.
            synchronized (this)
            {
                nextBuild++;
            }
    
            for(Subscription subscription: subscriptions)
            {
                if(subscription.conditionSatisfied(currentBuild))
                {
                    subscription.getContactPoint().notify(currentBuild);
                }
            }
            
            buildComplete();
        }        
    }

    /**
     * Tests if this project has a post-processor of the given name.
     * 
     * @param name
     *        the name to test for
     * @return true iff this project has a post-processor of the given name
     */
    public boolean hasPostProcessor(String name)
    {
        return postProcessors.containsKey(name);
    }
    
    /**
     * Returns the post-processor of the given name.
     * 
     * @param name
     *        the name of the post-processor to retrieve
     * @return the post-processor of the given name, or null if there is no
     *         post-processor by that name
     */
    public PostProcessorCommon getPostProcessor(String name)
    {
        return postProcessors.get(name);
    }
    
    /**
     * @return the registry of categories configured for this project
     */
    public FeatureCategoryRegistry getCategoryRegistry()
    {
        return categoryRegistry;
    }
    
    /**
     * @return the result of the current running or last completed build (if no build 
     * is running).
     */
    public BuildResult getCurrentBuild()
    {
        return currentBuild;
    }
	
	/**
	 * Retrieves a history of recent builds of this project.  The history may
	 * be shorter than requested (even empty) if there have not been enough
	 * previous builds.
	 * 
	 * @param maxBuilds
	 *        the maximum number of results to return
	 * @return a list of recent build results, most recent first
	 */
	public List<BuildResult> getHistory(int maxBuilds)
	{
        int latestBuild;
        
        synchronized(this)
        {
            latestBuild = nextBuild - 1;
        }

        return getBuildManager().getHistory(this, latestBuild, maxBuilds);
	}

    /**
     * @return the build recipe associated with this project.
     */
    public Recipe getRecipe()
    {
        return getRecipe(defaultRecipe);
    }

    /**
     * @param recipeName
     * @return the requested recipe.
     */ 
    public Recipe getRecipe(String recipeName)
    {
        for (Recipe recipe: recipes)
        {
            if (recipe.getName().equals(recipeName))
            {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Get the list of recipes associated with this project.
     * @return
     */ 
    public List<Recipe> getRecipes()
    {
        return Collections.unmodifiableList(recipes);
    }

    /**
     * Add a new recipe to this project.
     * @param recipe
     */ 
    public void addRecipe(Recipe recipe)
    {
        if (getRecipe(recipe.getName()) != null)
        {
            // recipe name must be unique within a project.
            throw new IllegalArgumentException("The recipe name '"+recipe.getName()+"' already exists. " +
                    "The recipe name must be unique within a project.");
        }
        recipes.add(recipe);
    }
    
    public void setReference(String name, Reference r)
    {
        references.put(name, r);
    }
    
    public Reference getReference(String name)
    {
        return references.get(name);
    }
    
    public void setProperty(String name, String value)
    {
        properties.put(name, value);
    }
    
    public String getProperty(String name)
    {
        return properties.get(name);
    }
    
    public String getProperty(String name, String defaultValue)
    {
        if (properties.containsKey(name))
        {
            return properties.get(name);
        }
        return defaultValue;
    }
    
    public Schedule getSchedule(String name)
    {
        for (Schedule s: schedules)
        {
            if (s.getName().equals(name))
            {
                return s;
            }
        }
        return null;
    }
    
    public void addSchedule(Schedule schedule)
    {
        schedules.add(schedule);
    }
    
    /**
     * Retrieves the result of the build with the given ID.
     * 
     * @param id
     *        id of the build result to load
     * @return the result of the build, or null if not found
     */
    public BuildResult getBuildResult(int id)
    {
        return getBuildManager().getBuildResult(this, id);
    }
    
    public boolean running()
    {
        return state == State.IDLE || state == State.BUILDING;
    }
    
    public synchronized void pause()
    {
        if(state == State.BUILDING)
        {
            state = State.PAUSING;
        }
        else if(state == State.IDLE)
        {
            state = State.PAUSED;
        }
    }
    
    public synchronized void resume()
    {
        if(state == State.PAUSING)
        {
            state = State.BUILDING;
        }
        else if(state == State.PAUSED)
        {
            state = State.IDLE;
        }
    }

    //=======================================================================
    // Old configuration implementation.
    //=======================================================================

    public void schedule()
    {
        // schedule job.
        int i = 0;
        for (Schedule schedule : schedules)
        {
            i++;
            Scheduler scheduler = QuartzManager.getScheduler();
            try
            {
                CronTrigger trigger = new CronTrigger(getName() + " Trigger." + i, Scheduler.DEFAULT_GROUP, "0 0 12 ? * *");
                JobDetail job = new JobDetail("build project " + name, Scheduler.DEFAULT_GROUP, BuildProject.class);
                job.getJobDataMap().put("project", this);
                scheduler.scheduleJob(job, trigger);
            }
            //TODO: review the following exceptions, they should not be RuntimeExceptions...
            catch (SchedulerException e)
            {
                throw new RuntimeException("Error scheduling job build: " + e.getMessage(), e);
            }
            catch (ParseException e)
            {
                throw new RuntimeException("Invalid cron expression:", e);
            }
        }
    }

    void setNextBuildId(int i)
    {
        nextBuild = i;
        List<BuildResult> history = getHistory(1);
        if(history.size() > 0)
        {
            currentBuild = history.get(0);
        }
    }

    public String replaceVariables(String input) throws BobException
    {
        return VariableHelper.replaceVariables(input, properties);
    }
}
