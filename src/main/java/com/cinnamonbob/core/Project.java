package com.cinnamonbob.core;

import com.cinnamonbob.util.FileSystemUtils;
import com.thoughtworks.xstream.XStream;
import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    private List<ContactPoint> subscriptions;
    /**
     * Directory for storing all project state.
     */
    private File projectDir;
    /**
     * Directory for storing build results.
     */
    private File buildsDir;
    /**
     * Directory used as a work area for the project.
     */
    private File workDir;
    /**
     * Identifier for the next build.
     */
    private int nextBuild;
    /**
     * Reference back to the boss.
     */
    private Bob theBuilder;
    /**
     * Used for (de)serialisation.
     */
    private XStream xstream;
        
    //=======================================================================
    // Implementation
    //=======================================================================

    private void addBuiltinVariables(ConfigContext context)
    {
        context.setVariable(VARIABLE_WORK_DIR, workDir.getAbsolutePath());
    }
	
    
    private BuildResult executeBuild()
    {
        // Allocate the result with a unique id.
        BuildResult result = new BuildResult(name, nextBuild, categoryRegistry);
        long startTime = System.currentTimeMillis();
        File buildDir = null;
        
        try
        {
            cleanWorkDir();
            buildDir = createBuildDir(buildsDir, result);            
        }
        catch(InternalBuildFailureException e)
        {
            // Not even able to create the build directory: bad news.
            result.setInternalFailure(e);
            logInternalBuildFailure(result);
            return result;
        }
        
        // May record internal failures too.
        executeCommands(result, buildDir);
        result.stamp(new TimeStamps(startTime, System.currentTimeMillis()));
        
        try
        {
            saveBuildResult(buildDir, result);
        }
        catch(InternalBuildFailureException e)
        {
            // We basically can't save anything about this, so bail out.
            // Don't clobber earlier failure...
            if(result.getInternalFailure() == null)
            {
                result.setInternalFailure(e);
                logInternalBuildFailure(result);
            }
        }
        
        // Don't increment nextBuild until we have finished the build, this
        // way the build won't be picked up by getHistory until complete.
        synchronized(this)
        {
            nextBuild++;
        }
        
        return result;
    }

    
    private void cleanWorkDir() throws InternalBuildFailureException
    {
        if(workDir.exists())
        {
            if(!FileSystemUtils.removeDirectory(workDir))
            {
                throw new InternalBuildFailureException("Could not clean work directory '" + workDir.getAbsolutePath() + '"');
            }
        }
        
        if(!workDir.mkdir())
        {
            throw new InternalBuildFailureException("Could not create work directory '" + workDir.getAbsolutePath() + "'");
        }
    }


    private void logInternalBuildFailure(BuildResult result)
    {
        InternalBuildFailureException e = result.getInternalFailure();
        
        LOG.severe("Project '" + name + "' build " + Integer.toString(result.getId()) + ": Internal build failure:");
        LOG.severe(e.getMessage());
        
        if(e.getCause() != null)
        {
            LOG.severe("Cause: " + e.getCause().getMessage());
        }
    }


    private void executeCommands(BuildResult result, File buildDir)
    {
        try
        {
            int i = 0;
            boolean failed = false;
            
            for(CommandCommon command: recipe)
            {
                File commandOutputDir = createCommandOutputDir(buildDir, command, i);
                
                if(!failed || command.getForce())
                {
                    CommandResultCommon commandResult = command.execute(commandOutputDir);
                
                    result.addCommandResult(commandResult);
                    saveCommandResult(commandOutputDir, commandResult);
                    i++;
                    
                    if(!commandResult.getResult().succeeded())
                    {
                        failed = true;
                    }
                }
            }
        }
        catch(InternalBuildFailureException e)
        {
            result.setInternalFailure(e);
            logInternalBuildFailure(result);
        }
    }


    private void saveBuildResult(File buildDir, BuildResult result) throws InternalBuildFailureException
    {
        File resultFile = new File(buildDir, RESULT_FILE_NAME);
        
        try
        {
            xstream.toXML(result, new FileWriter(resultFile));
        }
        catch(IOException e)
        {
            throw new InternalBuildFailureException("Could not save build result to file '" + resultFile.getAbsolutePath() + "'", e);
        }
    }


    private void saveCommandResult(File commandOutputDir, CommandResultCommon commandResult) throws InternalBuildFailureException
    {
        File resultFile = new File(commandOutputDir, RESULT_FILE_NAME);
        
        try
        {
            xstream.toXML(commandResult, new FileWriter(resultFile));
        }
        catch(IOException e)
        {
            throw new InternalBuildFailureException("Could not save command result to file '" + resultFile.getAbsolutePath() + "'", e);
        }
    }

    
    private File createBuildDir(File outputDir, BuildResult buildResult) throws InternalBuildFailureException
    {
        File buildDir = getBuildDir(outputDir, buildResult.getId());
        
        if(!buildDir.mkdir())
        {
            throw new InternalBuildFailureException("Could not create build directory '" + buildDir.getAbsolutePath() + "'");
        }
        
        return buildDir;
    }


    private File createCommandOutputDir(File buildDir, CommandCommon command, int index) throws InternalBuildFailureException
    {
        String dirName        = String.format("%08d-%s", index, command.getName());
        File commandOutputDir = new File(buildDir, dirName);
        
        if(!commandOutputDir.mkdir())
        {
            throw new InternalBuildFailureException("Could not create command output directory '" + commandOutputDir.getAbsolutePath() + "'");
        }
        
        return commandOutputDir;
    }

    
    private File getBuildDir(File outputDir, int buildId)
	{
        String dirName = String.format("%08d", new Integer(buildId));
        return new File(outputDir, dirName);
	}

    
    private void loadCommandResults(File buildDir, BuildResult result)
    {
        if(buildDir.isDirectory())
        {
            String files[] = buildDir.list();
            Arrays.sort(files);
            
            for(String dirName: files)
            {
                File dir = new File(buildDir, dirName);
                
                if(dir.isDirectory())
                {
                    File resultFile = new File(dir, "result.xml");
                    
                    try
                    {
                        CommandResultCommon commandResult = (CommandResultCommon)xstream.fromXML(new FileReader(resultFile));
                        result.addCommandResult(commandResult);
                    }
                    catch(FileNotFoundException e)
                    {
                        LOG.warning("I/O error loading command result from file '" + resultFile.getAbsolutePath() + "': " + e.getMessage());
                    }                    
                }
            }
        }
    }

   
	private BuildResult loadBuild(int buildId)
	{
		File        buildDir   = getBuildDir(buildsDir, buildId);
        File        resultFile = new File(buildDir, RESULT_FILE_NAME);
        BuildResult result     = null;
        
        try
        {
            result = (BuildResult)xstream.fromXML(new FileReader(resultFile));
            result.load(name, buildId, buildDir);            
            loadCommandResults(buildDir, result);
        }
        catch(IOException e)
        {
            LOG.warning("I/O error loading build result from file '" + resultFile.getAbsolutePath() + "'");
        }
        
        return result;
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

    
    private void loadConfig(String filename) throws ConfigException
    {
        Document      doc      = XMLConfigUtils.loadFile(filename);
        ConfigContext context  = new ConfigContext(filename);

        addBuiltinVariables(context);
        
        List<Element> elements = XMLConfigUtils.getElements(context, doc.getRootElement(), Arrays.asList(XMLConfigUtils.CONFIG_ELEMENT_PROPERTY, CONFIG_ELEMENT_DESCRIPTION, CONFIG_ELEMENT_POST_PROCESSOR, CONFIG_ELEMENT_RECIPE));
        
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
            else
            {
                assert(false);
            }
        }
    }
    
    //=======================================================================
    // Construction
    //=======================================================================

    public Project(Bob theBuilder, String name, String filename) throws ConfigException
    {
        this.theBuilder       = theBuilder;
        this.name             = name;
        this.postProcessors   = new TreeMap<String, PostProcessorCommon>();
        this.subscriptions    = new LinkedList<ContactPoint>();
        this.categoryRegistry = new FeatureCategoryRegistry();
        this.projectDir       = new File(theBuilder.getProjectRoot(), name);
        this.buildsDir        = new File(projectDir, DIR_BUILDS);
        this.workDir          = new File(projectDir, DIR_WORK);
        this.xstream          = new XStream();
        
        loadConfig(filename);
        
        // Determine next build id
        if(buildsDir.isDirectory())
        {
            String files[] = buildsDir.list();
            int    max     = -1;
            
            for(int i = 0; i < files.length; i++)
            {
                try
                {
                    int buildNumber = Integer.parseInt(files[i]);
                    
                    if(buildNumber > max)
                    {
                        max = buildNumber;
                    }
                }
                catch(NumberFormatException e)
                {
                    // Oh well, not a build dir
                }                
            }
            
            this.nextBuild = max + 1;
        }
        else
        {
            this.buildsDir.mkdirs();
            this.nextBuild = 0;
        }
    }

    //=======================================================================
    // Interface
    //=======================================================================

    /**
     * Adds a subscription to events on this project.
     * 
     * @param point
     *        the contact point to notify on project events
     */
    public void addSubscription(ContactPoint point)
    {
        subscriptions.add(point);
    }

    /**
     * @return the name of this project
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Executes a build of this project.
     * 
     * @return the result of the build
     */
    public BuildResult build()
    {
        BuildResult result = executeBuild();
        
        for(ContactPoint contact: subscriptions)
        {
            contact.notify(result);
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
		List<BuildResult> history = new LinkedList<BuildResult>();

		synchronized(this)
		{
			latestBuild = nextBuild - 1;
		}
		
		for(int i = latestBuild; i >= 0 && history.size() < maxBuilds; i--)
		{
			BuildResult result = loadBuild(i);
			if(result != null)
			{
				history.add(result);
			}
		}
		
		return history;
	}
}
