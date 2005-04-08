package com.cinnamonbob.core;

import com.thoughtworks.xstream.XStream;
import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A project describes a set of components and a recipe to "build" those
 * components.
 */
public class Project
{
    private static final String CONFIG_ELEMENT_DESCRIPTION = "description";
    private static final String CONFIG_ELEMENT_RECIPE      = "recipe";
    private static final String DIR_BUILDS                 = "builds";
    private static final String DIR_IN_PROGRESS            = "in-progress";
    private static final String STAMPS_FILE_NAME           = "stamps.xml";
    private static final String INTERNAL_FAILURE_FILE_NAME = "internal-failure.xml";
    private static final String RESULT_FILE_NAME           = "result.xml";
    
    /**
     * The name of this project, which must be unique amongst all projects.
     */
    private String name;
    /**
     * A human-readable description of this project.
     */
    private String description;
    /**
     * A description of how this project is built.
     */
    private Recipe recipe;
    /**
     * A description of when this project is built.
     */
    //private Schedule schedule;
    /**
     * The users involved in this project.
     */
    private List<ContactPoint> subscriptions;
    private Bob  theBuilder;
    private File projectDir;
    private File buildsDir;
    private int nextBuild;
    private Logger rootLogger;
    
    
    public Project(Bob theBuilder, String name, String filename) throws ConfigException
    {
        this.theBuilder = theBuilder;
        this.name = name;
        this.rootLogger = theBuilder.getRootLogger();
        this.subscriptions = new LinkedList<ContactPoint>();
        loadConfig(filename);
        this.projectDir = new File(theBuilder.getProjectRoot(), name);
        this.buildsDir = new File(projectDir, DIR_BUILDS);
        // FIXME
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


    private void loadConfig(String filename) throws ConfigException
    {
        Document      doc      = XMLConfigUtils.loadFile(filename);
        List<Element> elements = XMLConfigUtils.getElements(filename, doc.getRootElement(), Arrays.asList(CONFIG_ELEMENT_DESCRIPTION, CONFIG_ELEMENT_RECIPE));
        
        for(Element current: elements)
        {
            String  elementName = current.getLocalName();
                
            if(elementName.equals(CONFIG_ELEMENT_DESCRIPTION))
            {
                loadDescription(filename, current);
            }
            else if(elementName.equals(CONFIG_ELEMENT_RECIPE))
            {
                loadRecipe(filename, current);
            }
            else
            {
                assert(false);
            }
        }
    }


    private void loadDescription(String filename, Element element) throws ConfigException
    {
        description = XMLConfigUtils.getElementText(filename, element);
    }

    
    private void loadRecipe(String filename, Element element) throws ConfigException
    {
        recipe = new Recipe(filename, element, theBuilder.getCommandFactory());
    }


    public void addSubscription(ContactPoint point)
    {
        subscriptions.add(point);
    }


    /**
     * @return Returns the name of this project.
     */
    public String getName()
    {
        return name;
    }
    
    
    public BuildResult build(File outputDir)
    {
        BuildResult result = executeBuild(outputDir);
        
        for(ContactPoint contact: subscriptions)
        {
            contact.notify(result);
        }
        
        return result;
    }

    
    private BuildResult executeBuild(File outputDir)
    {
        // Allocate the result with a unique id.
        BuildResult result = new BuildResult(name, nextBuild++);
        long startTime = System.currentTimeMillis();
        File buildDir = null;
        
        try
        {
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
        
        return result;
    }

    
    private void logInternalBuildFailure(BuildResult result)
    {
        InternalBuildFailureException e = result.getInternalFailure();
        
        rootLogger.severe("Project '" + name + "' build " + Integer.toString(result.getId()) + ": Internal build failure:");
        rootLogger.severe(e.getMessage());
        
        if(e.getCause() != null)
        {
            rootLogger.severe("Cause: " + e.getCause().getMessage());
        }
    }


    private void executeCommands(BuildResult result, File buildDir)
    {
        try
        {
            int i = 0;
            XStream xstream = new XStream();
            
            for(CommandCommon command: recipe)
            {
                File                commandOutputDir = createCommandOutputDir(buildDir, command, i);
                CommandResultCommon commandResult    = command.execute(commandOutputDir);
                
                result.addCommandResult(commandResult);
                saveCommandResult(commandOutputDir, commandResult);
                i++;
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
        XStream xstream = new XStream();
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
        XStream xstream = new XStream();
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
        String dirName = String.format("%08d", new Integer(buildResult.getId()));
        File buildDir = new File(outputDir, dirName);
        
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
}
