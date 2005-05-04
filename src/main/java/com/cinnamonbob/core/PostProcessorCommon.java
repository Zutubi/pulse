package com.cinnamonbob.core;

import com.cinnamonbob.core.ext.ExtensionManagerUtils;
import nu.xom.Element;

import java.util.List;

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
    
    
    public PostProcessorCommon(ConfigContext context, Element element, Project project) throws ConfigException
    {
        name = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_NAME);

        List<Element> childElements = XMLConfigUtils.getElements(context, element);
        
        if(childElements.size() == 0)
        {
            throw new ConfigException(context.getFilename(), "Post processor '" + name + "' contains no child elements.");
        }
        
        // The first child is the specific command element
        postProcessor = ExtensionManagerUtils.createPostProcessor(childElements.get(0).getLocalName(), context, childElements.get(0), this, project);
    }
    
    
    public String getName()
    {
        return name;
    }


    public void process(Artifact artifact)
    {
        postProcessor.process(artifact);
    }


    public PostProcessor getProcessor()
    {
        return postProcessor;
    }
}
