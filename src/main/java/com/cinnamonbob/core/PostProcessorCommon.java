package com.cinnamonbob.core;

import java.util.List;

import nu.xom.Element;

/**
 * Contains the common part of a post processor.
 * 
 * @author jsankey
 */
public class PostProcessorCommon
{
    private static final String CONFIG_ATTR_NAME = "name";
    
    private String name;
    private PostProcessor postProcessor;
    
    
    public PostProcessorCommon(String filename, Element element, PostProcessorFactory factory, Project project) throws ConfigException
    {
        name = XMLConfigUtils.getAttributeValue(filename, element, CONFIG_ATTR_NAME);

        List<Element> childElements = XMLConfigUtils.getElements(filename, element);
        
        if(childElements.size() == 0)
        {
            throw new ConfigException(filename, "Post processor '" + name + "' contains no child elements.");
        }
        
        // The first child is the specific command element
        postProcessor = factory.createPostProcessor(childElements.get(0).getLocalName(), filename, childElements.get(0), this, project);
    }
    
    
    public String getName()
    {
        return name;
    }


    public void process(Artifact artifact)
    {
        postProcessor.process(artifact);
    }
}
