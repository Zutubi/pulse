package com.cinnamonbob.core2;

import com.cinnamonbob.core2.config.*;
import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.bootstrap.StartupManager;
import com.cinnamonbob.model.persistence.BuildResultDao;

import java.io.File;

/**
 * 
 *
 */
public class BuildProcessor
{
    public BuildResult execute(BuildRequest request) throws BuildException
    {
        Project project = lookupProject(request.getProjectName());
        
        BuildResultDao brdao = (BuildResultDao) StartupManager.getBean("buildResultDao");
        
        // allocate a build result to this request.
        BuildResult buildResult = new BuildResult();
        brdao.save(buildResult);
        buildResult.building();

        File buildDir = getBuildResultDir(buildResult);
        
        if (request.getRecipeName() != null)
        {
            project.build(buildResult, request.getRecipeName(), buildDir);
        }
        else
        {
            project.build(buildResult, buildDir);    
        }
        return buildResult;
    }

    /**
     * 
     * @param result
     * @return
     */ 
    private File getBuildResultDir(BuildResult result)
    {
        // root/<projectName>/builds/<buildId>
        
        File rootBuildDir = ConfigUtils.getManager().getAppConfig().getProjectRoot();
        File projectDir = new File(rootBuildDir, result.getProjectName());
        File buildsDir = new File(projectDir, "builds");
        
        File resultRootDir = new File(buildsDir, String.format("%08d", Long.valueOf(result.getId())));
        if (!resultRootDir.mkdirs())
        {
            throw new InternalBuildFailureException("Unable to create output directory");    
        }
        return resultRootDir;
    }
    
    private Project lookupProject(String name) throws BuildException
    {

        //TODO: move config into file.
        ProjectConfigurationLoader loader = new ProjectConfigurationLoader();        
        loader.register("property", Property.class);
        loader.register("description", Description.class);
        loader.register("recipe", Recipe.class);
        loader.register("schedule", Schedule.class);
        loader.register("def", ComponentDefinition.class);
        loader.register("post-processor", PostProcessorGroup.class);
        loader.register("command", CommandGroup.class);
        loader.register("cron", CronTrigger.class);
        loader.register("regex", RegexPostProcessor.class);
        loader.register("executable", ExecutableCommand.class);
        
        try 
        {
            return loader.load(getClass().getResourceAsStream(name + ".xml"));
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }
}
