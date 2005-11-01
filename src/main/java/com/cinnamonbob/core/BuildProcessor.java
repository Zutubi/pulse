package com.cinnamonbob.core;

import com.cinnamonbob.BuildRequest;
import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.model.*;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;
import com.cinnamonbob.util.FileSystemUtils;
import com.cinnamonbob.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public class BuildProcessor
{
    private static final Logger LOG = Logger.getLogger(BuildProcessor.class.getName());

    private ProjectManager projectManager;
    private BuildManager   buildManager;
    private SubscriptionManager subscriptionManager;
    private FileLoader fileLoader;

    public static String getBuildDirName(BuildResult result)
    {
        return String.format("%08d", Long.valueOf(result.getId()));
    }

    public static String getCommandDirName(CommandResult result)
    {
        return Long.toString(result.getId());
    }

    public static String getProjectDirName(Project project)
    {
        return Long.toString(project.getId());
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
        long number = buildManager.getNextBuildNumber(project.getName());

        BuildResult buildResult = new BuildResult(project.getName(), number);
        buildManager.save(buildResult);

        File rootBuildDir = ConfigUtils.getManager().getAppConfig().getProjectRoot();
        File projectDir = new File(rootBuildDir, getProjectDirName(project));
        File buildsDir = new File(projectDir, "builds");
        File buildDir = new File(buildsDir, getBuildDirName(buildResult));

        buildResult.commence(buildDir);

        try
        {
            File workDir = cleanWorkDir(projectDir);
            createBuildResultDir(buildDir);
            bootstrapBuild(project, buildResult, workDir, buildDir);

            BobFile bobFile = loadBobFile(workDir, project);

            build(project, bobFile, request.getRecipeName(), buildResult, buildDir);
        }
        catch(BuildException e)
        {
            e.printStackTrace();
            buildResult.error(e);
        }
        finally
        {
            buildResult.complete();
            buildManager.save(buildResult);
        }

        // sort out notifications.
        List<Subscription> subscriptions = subscriptionManager.getSubscriptions(project);
        for(Subscription subscription : subscriptions)
        {
            if (subscription.conditionSatisfied(buildResult))
            {
                subscription.getContactPoint().notify(project, buildResult);
            }
        }

        return buildResult;
    }

    public void build(Project project, BobFile bobFile, String recipeName, BuildResult buildResult, File outputDir) throws BuildException
    {
        Recipe recipe;

        if(recipeName == null)
        {
            recipeName = bobFile.getDefaultRecipe();
        }

        recipe = bobFile.getRecipe(recipeName);
        if (recipe == null)
        {
            throw new BuildException("Undefined recipe '" + recipeName + "' for project '" + project.getName() + "'");
        }

        // TODO: support continuing build when errors occur. Take care: exceptions.
        for (Command command : recipe.getCommands())
        {
            CommandResult result = new CommandResult(command.getName());

            buildResult.add(result);
            buildManager.save(buildResult);

            File commandOutput = new File(outputDir, getCommandDirName(result));
            result.commence(commandOutput);
            buildManager.save(buildResult);

            try
            {
                if(!commandOutput.mkdir())
                {
                    throw new BuildException("Could not create command output directory '" + commandOutput.getAbsolutePath() + "'");
                }

                command.execute(commandOutput, result);
            }
            catch(BuildException e)
            {
                result.error(e);
            }
            catch(Exception e)
            {
                LOG.log(Level.SEVERE, "Unhandled exception during build", e);
                result.error(new BuildException(e));
            }
            finally
            {
                result.complete();
                buildManager.save(buildResult);
            }

            switch(result.getState())
            {
                case FAILURE:
                    buildResult.failure();
                    return;
                case ERROR:
                    buildResult.commandError();
                    return;
            }
        }
    }

    private void bootstrapBuild(Project project, BuildResult result, File workDir, File resultDir) throws BuildException
    {
        List<Scm> scms = project.getScms();

        if(scms.size() == 0)
        {
            throw new BuildException("Project '" + project.getName() + "' has no SCMs configured.");
        }

        for(Scm scm: scms)
        {
            File scmDir = new File(workDir, scm.getPath());

            try
            {
                SCMServer server = scm.createServer();
                LinkedList<Change> changes = new LinkedList<Change>();
                Revision latestRevision = server.checkout(scmDir, null, changes);

                saveChanges(resultDir, scm, changes);

                List<Changelist> scmChanges = null;

                try
                {
                    BuildResult previousBuildResult = buildManager.getLatestBuildResult(project.getName());

                    if(previousBuildResult != null)
                    {
                        BuildScmDetails previousScmDetails = previousBuildResult.getScmDetails(scm.getId());
                        if(previousScmDetails != null)
                        {
                            Revision previousRevision = previousScmDetails.getRevision();
                            if (previousRevision != null)
                            {
                                scmChanges = server.getChanges(previousRevision, latestRevision, "");
                            }
                        }
                    }
                }
                catch (SCMException e)
                {
                    // TODO: need to report this failure to the user. However,
                    // this is not fatal to the current build
                    LOG.log(Level.WARNING, "Unable to retrieve changelist details from Scm server. ", e);
                }

                BuildScmDetails scmDetails = new BuildScmDetails(scm.getName(), latestRevision, scmChanges);
                result.addScmDetails(scm.getId(), scmDetails);
            }
            catch(SCMException e)
            {
                throw new BuildException(e);
            }
        }
    }

    private void saveChanges(File outputDir, Scm scm, LinkedList<Change> changes)
    {
        File       output = new File(outputDir, String.format("%d", Long.valueOf(scm.getId())) + ".changes");
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
            throw new BuildException("Could not create output file '" + output.getAbsolutePath() + "'", e);
        }
        finally
        {
            IOUtils.close(writer);
        }
    }

    private File cleanWorkDir(File projectDir)
    {
        File workDir = new File(projectDir, "work");

        if(workDir.exists())
        {
            if(!FileSystemUtils.removeDirectory(workDir))
            {
                throw new BuildException("Could not clean work directory '" + workDir.getAbsolutePath() + '"');
            }
        }

        if(!workDir.mkdirs())
        {
            throw new BuildException("Could not create work directory '" + workDir.getAbsolutePath() + "'");
        }

        return workDir;
    }

    private void createBuildResultDir(File buildDir)
    {
        if (!buildDir.mkdirs())
        {
            throw new BuildException("Unable to create build directory '" + buildDir.getAbsolutePath() + "'");
        }
    }

    private BobFile loadBobFile(File workDir, Project project) throws BuildException
    {
        List<Reference> properties = new LinkedList<Reference>();

        Property property = new Property("work.dir", workDir.getAbsolutePath());
        properties.add(property);

        try
        {
            BobFile         result = new BobFile();
            File            bob    = new File(workDir, project.getBobFile());
            FileInputStream stream = new FileInputStream(bob);

            fileLoader.load(stream, result, properties);
            return result;
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }

    public void setFileLoader(FileLoader fileLoader)
    {
        this.fileLoader = fileLoader;

        // TODO: move config into file.
        fileLoader.register("property", Property.class);
        fileLoader.register("recipe", Recipe.class);
        fileLoader.register("def", ComponentDefinition.class);
        fileLoader.register("post-processor", PostProcessorGroup.class);
        fileLoader.register("command", CommandGroup.class);
        fileLoader.register("regex", RegexPostProcessor.class);
        fileLoader.register("executable", ExecutableCommand.class);
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setSubscriptionManager(SubscriptionManager subscriptionManager)
    {
        this.subscriptionManager = subscriptionManager;
    }
}
