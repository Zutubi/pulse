package com.cinnamonbob.core.ext;

import com.cinnamonbob.bootstrap.StartupManager;
import com.cinnamonbob.core.*;
import nu.xom.Element;

/**
 * 
 *
 */
public class ExtensionManagerUtils
{

    private static final String BEAN_NAME = "extensionManager";

    public static ExtensionManager getManager()
    {
        return (ExtensionManager) StartupManager.getBean(BEAN_NAME);
    }

    public static Command createCommand(String name, ConfigContext context, Element element, CommandCommon common)
            throws ConfigException
    {
        return getManager().createCommand(name, context, element, common);
    }

    public static PostProcessor createPostProcessor(String name, ConfigContext context, Element element, PostProcessorCommon common, Project project)
            throws ConfigException
    {
        return getManager().createPostProcessor(name, context, element, common, project);
    }
}
