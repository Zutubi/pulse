package com.cinnamonbob.core;

import nu.xom.Element;

public class ProcessSpec
{
    private static final String CONFIG_ATTR_PROCESSOR = "processor";
    private static final String CONFIG_ATTR_ARTIFACT  = "artifact";
    
    private PostProcessorCommon processor;
    private ArtifactSpec        artifact;
    
    
    public ProcessSpec(ConfigContext context, Element element, Project project, CommandCommon command) throws ConfigException
    {
        String processorName = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_PROCESSOR);
        String artifactName  = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_ARTIFACT);
        
        if(project.hasPostProcessor(processorName))
        {
            processor = project.getPostProcessor(processorName);
        }
        else
        {
            throw new ConfigException(context.getFilename(), "Command '" + command.getName() + "' process directive refers to unknown post-processor '" + processorName + "'");
        }
        
        if(command.hasArtifact(artifactName))
        {
            artifact = command.getArtifact(artifactName);
        }
        else
        {
            throw new ConfigException(context.getFilename(), "Command '" + command.getName() + "' process directive refers to unknown artifact '" + artifactName + "'");            
        }
    }


    public ArtifactSpec getArtifact()
    {
        return artifact;
    }
    

    public PostProcessorCommon getProcessor()
    {
        return processor;
    }
    
}
