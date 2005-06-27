package com.cinnamonbob.core2;

import com.cinnamonbob.core2.config.*;

/**
 * 
 *
 */
public class BuildProcessor
{
    public BuildResult execute(BuildRequest request) throws BuildException
    {
        Project project = lookupProject(request.getProjectName());
        
        if (request.getRecipeName() != null)
        {
            return project.build(request.getRecipeName());
        }
        else
        {
            return project.build();    
        }
    }
    
    private Project lookupProject(String name) throws BuildException
    {
//        ApplicationPaths paths = BootstrapUtils.getManager().getApplicationPaths();
//        File configDir = paths.getUserConfigRoot();        
//        File projectXml = new File(configDir, name + ".xml");
//        if (!projectXml.exists())
//        {
//            throw new BuildException("Unknown project.xml: " + projectXml.getAbsolutePath());
//        }

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
