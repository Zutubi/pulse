package com.cinnamonbob;

import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.RecipeProcessor;
import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.events.build.BuildCommencedEvent;
import com.cinnamonbob.events.build.BuildCompletedEvent;
import com.cinnamonbob.events.build.BuildEvent;
import com.cinnamonbob.events.build.RecipeCompletedEvent;
import com.cinnamonbob.model.*;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;
import com.cinnamonbob.util.logging.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Processor for executing builds on the bob master.
 */
public class MasterBuildProcessor implements EventListener
{
    private static final Logger LOG = Logger.getLogger(MasterBuildProcessor.class.getName());

    private RecipeProcessor processor;
    private ProjectManager projectManager;
    private BuildManager buildManager;
    private SubscriptionManager subscriptionManager;
    private EventManager eventManager;

    public RecipeResult execute(BuildRequest request)
    {
        Project project = projectManager.getProject(request.getProjectName());
        if (project == null)
        {
            LOG.warning("Build request for unknown project '" + request.getProjectName() + "'");
            return null;
        }

        // allocate a build id to this request.
        long number = buildManager.getNextBuildNumber(project);

        BuildResult buildResult = new BuildResult(project, number);
        RecipeResult recipeResult = new RecipeResult(request.getRecipeName());
        buildResult.add(new RecipeResultNode(recipeResult));

        File rootBuildDir = ConfigUtils.getManager().getAppConfig().getProjectRoot();
        File projectDir = new File(rootBuildDir, getProjectDirName(project));
        File buildsDir = new File(projectDir, "builds");
        File buildDir = new File(buildsDir, getBuildDirName(buildResult));

        buildResult.commence(buildDir);
        eventManager.publish(new BuildCommencedEvent(this, buildResult));

        try
        {
            File workDir = cleanWorkDir(projectDir);
            if (!buildDir.mkdirs())
            {
                throw new BuildException("Unable to create build directory '" + buildDir.getAbsolutePath() + "'");
            }

            bootstrapBuild(project, buildResult, workDir, buildDir);

            processor.build(workDir, project.getBobFile(), request.getRecipeName(), recipeResult, buildDir);
        }
        catch (BuildException e)
        {
            LOG.severe("Build error", e);
            recipeResult.error(e);
        }
        finally
        {
            recipeResult.complete();
            eventManager.publish(new RecipeCompletedEvent(this, recipeResult));
            buildResult.complete();
            eventManager.publish(new BuildCompletedEvent(this, buildResult));
        }

        // sort out notifications.
        List<Subscription> subscriptions = subscriptionManager.getSubscriptions(project);
        for (Subscription subscription : subscriptions)
        {
            if (subscription.conditionSatisfied(buildResult))
            {
                subscription.getContactPoint().notify(project, buildResult);
            }
        }

        return recipeResult;
    }

    public static String getProjectDirName(Project project)
    {
        return Long.toString(project.getId());
    }

    public static String getBuildDirName(BuildResult result)
    {
        return String.format("%08d", Long.valueOf(result.getNumber()));
    }

    private File cleanWorkDir(File projectDir)
    {
        File workDir = new File(projectDir, "work");

        if (workDir.exists())
        {
            if (!FileSystemUtils.removeDirectory(workDir))
            {
                throw new BuildException("Could not clean work directory '" + workDir.getAbsolutePath() + '"');
            }
        }

        if (!workDir.mkdirs())
        {
            throw new BuildException("Could not create work directory '" + workDir.getAbsolutePath() + "'");
        }

        return workDir;
    }

    private void bootstrapBuild(Project project, BuildResult result, File workDir, File resultDir) throws BuildException
    {
        List<Scm> scms = project.getScms();

        if (scms.size() == 0)
        {
            throw new BuildException("Project '" + project.getName() + "' has no SCMs configured.");
        }

        for (Scm scm : scms)
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
                    BuildResult previousBuildResult = buildManager.getLatestBuildResult(project);

                    if (previousBuildResult != null)
                    {
                        BuildScmDetails previousScmDetails = previousBuildResult.getScmDetails(scm.getId());
                        if (previousScmDetails != null)
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
                    LOG.warning("Unable to retrieve changelist details from Scm server. ", e);
                }

                BuildScmDetails scmDetails = new BuildScmDetails(scm.getName(), latestRevision, scmChanges);
                result.addScmDetails(scm.getId(), scmDetails);
            }
            catch (SCMException e)
            {
                throw new BuildException(e);
            }
        }
    }

    private void saveChanges(File outputDir, Scm scm, LinkedList<Change> changes)
    {
        File output = new File(outputDir, String.format("%d", Long.valueOf(scm.getId())) + ".changes");
        FileWriter writer = null;

        try
        {
            writer = new FileWriter(output);

            for (Change change : changes)
            {
                writer.write(change.getFilename() + "#" + change.getRevision() + "\n");
            }
        }
        catch (IOException e)
        {
            throw new BuildException("Could not create output file '" + output.getAbsolutePath() + "'", e);
        }
        finally
        {
            IOUtils.close(writer);
        }
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

    public void setBuildProcessor(RecipeProcessor processor)
    {
        this.processor = processor;
    }

    public void handleEvent(Event event)
    {
        buildManager.save(((BuildEvent) event).getResult());
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
        eventManager.register(this);
    }
}
