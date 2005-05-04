package com.cinnamonbob.core.ext;

import com.cinnamonbob.core.*;
import nu.xom.Element;

/**
 * 
 *
 */
public interface ExtensionManager
{

    /**
     * Retrieve the specified command definition.
     * @param name
     * @return
     */
    Class getCommandDefinition(String name);

    /**
     *
     * @param name
     * @param context
     * @param element
     * @param common
     * @return
     * @throws ConfigException
     */
    Command createCommand(String name, ConfigContext context, Element element, CommandCommon common)
            throws ConfigException;

    Class getPostProcessorDefinition(String name);

    PostProcessor createPostProcessor(String name, ConfigContext context, Element element, PostProcessorCommon common, Project project)
            throws ConfigException;
}
