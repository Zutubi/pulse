package com.cinnamonbob.core2;

import com.cinnamonbob.BuildRequest;
import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.core2.InternalBuildFailureException;
import com.cinnamonbob.core2.config.*;
import com.cinnamonbob.model.*;
import com.cinnamonbob.scm.*;
import com.cinnamonbob.util.FileSystemUtils;
import com.cinnamonbob.util.IOHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 */
public class BuildProcessor
{
    private static final Logger LOG = Logger.getLogger(BuildProcessor.class.getName());

    private ProjectManager projectManager;
    private BuildManager   buildManager;
    private UserManager    userManager;
    
    public BuildProcessor()
    {
        // TODO make this a singleton managed by Spring??
        projectManager = (ProjectManager)ComponentContext.getBean("projectManager");
        buildManager = (BuildManager)ComponentContext.getBean("buildManager");
        userManager = (UserManager)ComponentContext.getBean("userManager");
    }
    
    public BuildResult execute(BuildRequest request)
    {
        Project project = projectManager.getProject(request.getProjectName());
        
        if(project == null)
        {
            LOG.warning("Build request for unknown project '" + request.getProjectName() + "'");
            return null;
        }
        
        // allocate a build result to this request.
        long              number = 1;
        List<BuildResult> builds = buildManager.getLatestBuildResultsForProject(project.getName(), 1);
        
        if(builds.size() > 0)
        {
            number = builds.get(0).getNumber() + 1;
        }

        BuildResult buildResult = new BuildResult(project.getName(), number);
        buildManager.save(buildResult);
        buildResult.building();

        try
        {    
            File rootBuildDir = ConfigUtils.getManager().getAppConfig().getProjectRoot();
            File projectDir = new File(rootBuildDir, buildResult.getProjectName());
    
            File workDir  = cleanWorkDir(projectDir);
            File buildDir = createBuildResultDir(projectDir, buildResult);
            File scmDir   = bootstrapBuild(project, buildResult, workDir, buildDir);
            
            BobFile bobFile = loadBobFile(scmDir);
            
            build(bobFile, request.getRecipeName(), buildResult, buildDir);
        }
        catch(BuildException e)
        {
            // TODO: store details???
            buildResult.setSucceeded(false);
        }
        catch(InternalBuildFailureException e)
        {
            // TODO: store details???
            LOG.severe(e.toString());
            buildResult.setSucceeded(false);
        }
        finally
        {
            buildResult.completed();
            buildManager.save(buildResult);
        }
        
        // FIXME what a hack!
        for(Object o: userManager.getUsersWithLoginLike("%"))
        {
            User u = (User)o;
            for(ContactPoint c: u.getContactPoints())
            {
                c.notify(project, buildResult);
            }
        }
        
        return buildResult;
    }
        
    public void build(BobFile bobFile, String recipeName, BuildResult buildResult, File outputDir) throws BuildException
    {
        Recipe recipe;
        
        if(recipeName == null)
        {
            recipeName = bobFile.getDefaultRecipe();
        }
        
        recipe = bobFile.getRecipe(recipeName);
        if (recipe == null)
        {
            // TODO problematic to report: no name atm
            throw new BuildException("Undefined recipe " + recipeName);
        }
        
        try 
        {
            //TODO: support continuing build when errors occur.
            int i = 0;
            for (Command command : recipe.getCommands())
            {
                //TODO: should name these directory a little better. ie: if we
                //TODO: are dealing with a named command, then include the name.
                //TODO: should all commands be named?
                File commandOutput = new File(outputDir, String.format("%08d", i++));
                
                if(!commandOutput.mkdir())
                {
                    throw new InternalBuildFailureException("Could not create command output directory '" + commandOutput.getAbsolutePath() + "'");
                }
                //TODO: need to associate this command result with the id 'i' so that we
                //TODO: have a name to uniquely identify a particular command result from within
                //TODO: a build result.
                CommandResult result = command.execute(commandOutput);
                buildResult.add(result);
                if (!result.succeeded())
                {
                    buildResult.setSucceeded(false);
                    return;
                }
            }
            buildResult.setSucceeded(true);
        }
        catch (CommandException e)
        {
            throw new BuildException(e);
        }
    }

    private File bootstrapBuild(Project project, BuildResult result, File workDir, File resultDir) throws BuildException
    {
        List<Scm> scms = project.getScms();
        
        if(scms.size() != 1)
        {
            // TODO: handle 0 and multi scm
            throw new InternalBuildFailureException("I don't support that yet!");
        }
        
        Scm  scm    = scms.get(0);
        File scmDir = new File(workDir, scm.getPath());
        
        try
        {
            SCMServer          server   = scm.createServer();
            LinkedList<Change> changes  = new LinkedList<Change>();
            Revision           revision = server.checkout(scmDir, null, changes);
            
            result.setRevision(revision.toString());
            saveChanges(resultDir, changes);
        }
        catch(SCMException e)
        {
            throw new BuildException(e);
        }
        
        return scmDir;
    }
    
    private void saveChanges(File outputDir, LinkedList<Change> changes) throws InternalBuildFailureException
    {
        // TODO: name needs to change for multi-scm
        File       output = new File(outputDir, "changes");
        FileWriter writer = null;
        
        try
        {
            writer = new FileWriter(output);

            for(Change change: changes)
            {
                writer.write(change.getFilename() + "#" + change.getRevision() + "\n");
            }
        }
        catch(IOException e)
        {
            throw new InternalBuildFailureException("Could not create output file '" + output.getAbsolutePath() + "'", e);
        }
        finally
        {
            IOHelper.close(writer);
        }
    }

    private File cleanWorkDir(File projectDir)
    {
        File workDir = new File(projectDir, "work");

        if(workDir.exists())
        {
            if(!FileSystemUtils.removeDirectory(workDir))
            {
                throw new InternalBuildFailureException("Could not clean work directory '" + workDir.getAbsolutePath() + '"');
            }
        }

        if(!workDir.mkdirs())
        {
            throw new InternalBuildFailureException("Could not create work directory '" + workDir.getAbsolutePath() + "'");
        }

        return workDir;
    }

    private File createBuildResultDir(File projectDir, BuildResult result)
    {
        // root/<projectName>/builds/<buildId>        
        File buildsDir = new File(projectDir, "builds");
        
        File resultRootDir = new File(buildsDir, String.format("%08d", Long.valueOf(result.getId())));
        if (!resultRootDir.mkdirs())
        {
            throw new InternalBuildFailureException("Unable to create output directory");    
        }
        return resultRootDir;
    }
    
    private BobFile loadBobFile(File scmDir) throws BuildException
    {
        // TODO: move config into file.
        BobFileLoader loader = new BobFileLoader();        

        loader.register("property", Property.class);
        loader.register("recipe", Recipe.class);
        loader.register("def", ComponentDefinition.class);
        loader.register("post-processor", PostProcessorGroup.class);
        loader.register("command", CommandGroup.class);
        loader.register("cron", CronTrigger.class);
        loader.register("regex", RegexPostProcessor.class);
        loader.register("executable", ExecutableCommand.class);

        Map<String, String> properties = new TreeMap<String, String>();
        
        properties.put("work.dir", scmDir.getAbsolutePath());
        
        try 
        {
            File            bob    = new File(scmDir, "bob.xml");
            FileInputStream stream = new FileInputStream(bob);
            
            return loader.load(stream, properties);
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }
}
