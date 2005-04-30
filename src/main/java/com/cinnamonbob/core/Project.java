package com.cinnamonbob.core;

import nu.xom.Document;
import nu.xom.Element;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.text.ParseException;

import com.cinnamonbob.bootstrap.quartz.QuartzManager;
import org.quartz.*;

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
    /**
     * A description of how this project is built.
     */
    private Recipe recipe;
    /**
     * A description of when this project is built.
     */
    //private Schedule schedule;
    /**
     * Subscriptions to events on this project.
     */
    private List<Subscription> subscriptions;
    /**
     * Reference back to the boss.
     */
    private Bob theBuilder;
    /**
     * The next availabel build id.
     */
    private int nextBuild;
    
    private Schedule schedule;

    //=======================================================================
    // Types
    //=======================================================================

    public enum Event
    {
        BUILD_COMPLETE,
        BUILD_FAILED
    }
    
    //=======================================================================
    // Implementation
    //=======================================================================

    private void addBuiltinVariables(ConfigContext context)
    {
        context.setVariable(VARIABLE_WORK_DIR, theBuilder.getBuildManager().getWorkRoot(this).getAbsolutePath());
    }

    
    private void loadDescription(ConfigContext context, Element element) throws ConfigException
    {
        description = XMLConfigUtils.getElementText(context, element);
    }


    private void loadPostProcessor(ConfigContext context, Element element) throws ConfigException
    {
        PostProcessorCommon post = new PostProcessorCommon(context, element, theBuilder.getPostProcessorFactory(), this);
        
        if(postProcessors.containsKey(post.getName()))
        {
            throw new ConfigException(context.getFilename(), "Project '" + name + "' already contains a post-processor named '" + post.getName() + "'");
        }
        
        postProcessors.put(post.getName(), post);
    }
    
    private void loadRecipe(ConfigContext context, Element element) throws ConfigException
    {
        recipe = new Recipe(context, element, theBuilder.getCommandFactory(), this);
    }

    /**
     *
     * @param context
     * @param element
     * @throws ConfigException
     */
    private void loadSchedule(ConfigContext context, Element element) throws ConfigException
    {
        schedule = new Schedule(context, element, this);

        // schedule job.
        Scheduler scheduler = QuartzManager.getScheduler();
        try
        {
            CronTrigger trigger = new CronTrigger(getName() + " Trigger.", Scheduler.DEFAULT_GROUP, schedule.getCronSchedule());
            JobDetail job = new JobDetail("build project " + name, Scheduler.DEFAULT_GROUP, BuildProject.class);
            job.getJobDataMap().put("project", this);
            scheduler.scheduleJob(job, trigger);
        }
        catch (SchedulerException e)
        {
            throw new ConfigException("", "Error scheduling job build: " + e.getMessage());
        } catch (ParseException e)
        {
            throw new ConfigException(context.getFilename(), "Invalid cron expression: " + schedule.getCronSchedule());
        }
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
    }
    
    // TODO create work path
    // TODO internal failure loading
    
    //=======================================================================
    // Construction
    //=======================================================================

    public Project(Bob theBuilder, String name, String filename) throws ConfigException
    {
        this.theBuilder       = theBuilder;
        this.name             = name;
        this.postProcessors   = new TreeMap<String, PostProcessorCommon>();
        this.subscriptions    = new LinkedList<Subscription>();
        this.categoryRegistry = new FeatureCategoryRegistry();
        this.nextBuild        = theBuilder.getBuildManager().determineNextAvailableBuildId(this);        

        loadConfig(filename);
    }

    //=======================================================================
    // Interface
    //=======================================================================
    
    /**
     * Adds a subscription to events on this project.
     * 
     * @param event
     *        the event to subscribe to
     * @param point
     *        the contact point to notify on project events
     */
    public void addSubscription(Event event, ContactPoint point)
    {
        subscriptions.add(new Subscription(event, point));
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
        BuildResult result = theBuilder.getBuildManager().executeBuild(this, nextBuild);
        
        // Don't increment nextBuild until we have finished the build, this
        // way the build won't be picked up by getHistory until complete.
        synchronized (this)
        {
            nextBuild++;
        }

        for(Subscription subscription: subscriptions)
        {
            boolean notify;
            
            if(result.succeeded())
            {
                notify = subscription.getEventType() == Event.BUILD_COMPLETE;
            }
            else
            {
                notify = true;
            }
            
            if(notify)
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

        return theBuilder.getBuildManager().getHistory(this, latestBuild, maxBuilds);
	}

    /**
     * @return the build recipe associated with this project.
     */
    public Recipe getRecipe()
    {
        return recipe;
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
        return theBuilder.getBuildManager().getBuildResult(this, id);
    }
}
