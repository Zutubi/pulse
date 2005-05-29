package com.cinnamonbob.core;

import com.cinnamonbob.bootstrap.quartz.QuartzManager;
import nu.xom.Document;
import nu.xom.Element;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * A project describes a set of components and a recipe to "build" those
 * components.
 */
public class Project
{
    private static final Logger LOG = Logger.getLogger(Project.class.getName());

    private static final String CONFIG_ELEMENT_DESCRIPTION    = "description";
    private static final String CONFIG_ELEMENT_POST_PROCESSOR = "post-processor";
    private static final String CONFIG_ELEMENT_RECIPE         = "recipe";
    private static final String CONFIG_ELEMENT_SCHEDULE       = "schedule";

    private static final String DIR_BUILDS                    = "builds";
    private static final String DIR_WORK                      = "work";
    private static final String DIR_IN_PROGRESS               = "in-progress";
    private static final String STAMPS_FILE_NAME              = "stamps.xml";
    private static final String INTERNAL_FAILURE_FILE_NAME    = "internal-failure.xml";
    private static final String RESULT_FILE_NAME              = "result.xml";
    private static final String VARIABLE_WORK_DIR             = "work.dir";

    /**
     * The name of this project, which must be unique amongst all projects.
     */
    private String name;
    /**
     * A human-readable description of this project.
     */
    private String description;
    /**
     * Registry of known feature categories.
     */
    private FeatureCategoryRegistry categoryRegistry;
    /**
     * Post-processors defined for this project.
     */
    private Map<String, PostProcessorCommon> postProcessors;
    
    private List<Recipe> recipes = new LinkedList<Recipe>();
    
    private String defaultRecipe = "";
    
    /**
     * A description of when this project is built.
     */
    //private Schedule schedule;
    /**
     * Subscriptions to events on this project.
     */
    private List<Subscription> subscriptions;

    /**
     * The next availabel build id.
     */
    private int nextBuild;
    
    private List<Schedule> schedules = new LinkedList<Schedule>();
    
    //=======================================================================
    // Implementation
    //=======================================================================

    private void addBuiltinVariables(ConfigContext context)
    {
        context.setVariable(VARIABLE_WORK_DIR, getBuildManager().getWorkRoot(this).getAbsolutePath());
    }


    private void loadDescription(ConfigContext context, Element element) throws ConfigException
    {
        description = XMLConfigUtils.getElementText(context, element);
    }


    private void loadPostProcessor(ConfigContext context, Element element) throws ConfigException
    {
        PostProcessorCommon post = new PostProcessorCommon(context, element, this);

        if(postProcessors.containsKey(post.getName()))
        {
            throw new ConfigException(context.getFilename(), "Project '" + name + "' already contains a post-processor named '" + post.getName() + "'");
        }

        postProcessors.put(post.getName(), post);
    }

    private void loadRecipe(ConfigContext context, Element element) throws ConfigException
    {
        recipes.add(new Recipe(context, element, this));
    }

    /**
     *
     * @param context
     * @param element
     * @throws ConfigException
     */
    private void loadSchedule(ConfigContext context, Element element) throws ConfigException
    {
        schedules.add(new Schedule(context, element, this));
    }

    private void loadConfig(String filename) throws ConfigException
    {
        Document      doc      = XMLConfigUtils.loadFile(filename);
        ConfigContext context  = new ConfigContext(filename);

        addBuiltinVariables(context);

        List<Element> elements = XMLConfigUtils.getElements(context, doc.getRootElement(), Arrays.asList(XMLConfigUtils.CONFIG_ELEMENT_PROPERTY, CONFIG_ELEMENT_DESCRIPTION, CONFIG_ELEMENT_POST_PROCESSOR, CONFIG_ELEMENT_RECIPE, CONFIG_ELEMENT_SCHEDULE));

        XMLConfigUtils.extractProperties(context, elements);

        for(Element current: elements)
        {
            String  elementName = current.getLocalName();

            if(elementName.equals(CONFIG_ELEMENT_DESCRIPTION))
            {
                loadDescription(context, current);
            }
            else if(elementName.equals(CONFIG_ELEMENT_POST_PROCESSOR))
            {
                loadPostProcessor(context, current);
            }
            else if(elementName.equals(CONFIG_ELEMENT_RECIPE))
            {
                loadRecipe(context, current);
            }
            else if (elementName.equals(CONFIG_ELEMENT_SCHEDULE))
            {
                loadSchedule(context, current);
            }
            else
            {
                assert(false);
            }
        }
        
        schedule();
    }

    private void schedule()
    {
        // schedule job.
        int i = 0;
        for (Schedule schedule : schedules)
        {
            i++;
            Scheduler scheduler = QuartzManager.getScheduler();
            try
            {
                CronTrigger trigger = new CronTrigger(getName() + " Trigger." + i, Scheduler.DEFAULT_GROUP, schedule.getCronSchedule());
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
                throw new RuntimeException("Invalid cron expression: " + schedule.getCronSchedule(), e);
            }
        }
    }

    //=======================================================================
    // Construction
    //=======================================================================

    public Project(String name, String filename) throws ConfigException
    {
        this.name             = name;
        this.postProcessors   = new TreeMap<String, PostProcessorCommon>();
        this.subscriptions    = new LinkedList<Subscription>();
        this.categoryRegistry = new FeatureCategoryRegistry();
        this.nextBuild        = getBuildManager().determineNextAvailableBuildId(this);        

        loadConfig(filename);
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
     * @return the name of this project
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * @return the description of this project
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Executes a build of this project.
     * 
     * @return the result of the build
     */
    public BuildResult build()
    {
        BuildResult result = getBuildManager().executeBuild(this, nextBuild);
        
        // Don't increment nextBuild until we have finished the build, this
        // way the build won't be picked up by getHistory until complete.
        synchronized (this)
        {
            nextBuild++;
        }

        for(Subscription subscription: subscriptions)
        {
            if(subscription.conditionsSatisfied(result))
            {
                subscription.getContactPoint().notify(result);
            }
        }
        
        return result;
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
    
    private BuildManager getBuildManager()
    {
        return BuildManager.getInstance();
    }
}
