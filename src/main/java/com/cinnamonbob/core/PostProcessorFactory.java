package com.cinnamonbob.core;

import nu.xom.Element;


/**
 * @author jsankey
 */
public class PostProcessorFactory extends GenericFactory<PostProcessor>
{
    public PostProcessorFactory()
    {
        super(PostProcessor.class, PostProcessorCommon.class, Project.class);
    }
    
    public PostProcessor createPostProcessor(String name, String filename, Element element, PostProcessorCommon common, Project project) throws ConfigException
    {
        return (PostProcessor)super.create(name, filename, element, common, project);
    }
}
