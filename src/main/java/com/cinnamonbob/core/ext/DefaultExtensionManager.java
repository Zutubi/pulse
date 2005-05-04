package com.cinnamonbob.core.ext;

import com.cinnamonbob.core.*;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import nu.xom.Element;

/**
 * 
 *
 */
public class DefaultExtensionManager implements ExtensionManager
{
    private static final Logger LOG = Logger.getLogger(DefaultExtensionManager.class.getName());

    private Map<String, Class> commandDefinitions = new HashMap<String, Class>();
    private CommandFactory commandFactory = new CommandFactory();

    private Map<String, Class> postProcessorDefinitions = new HashMap<String, Class>();
    private PostProcessorFactory postProcessorFactory = new PostProcessorFactory();



    public void init() throws IOException
    {
        initType("commands.properties", commandDefinitions, commandFactory);
        initType("postprocessors.properties", postProcessorDefinitions, postProcessorFactory);
    }

    private void initType(String propertyFileName, Map<String, Class> definitions, GenericFactory factory) throws IOException
    {
        InputStream input = getClass().getResourceAsStream(propertyFileName);
        Properties props = new Properties();
        props.load(input);

        // retrieve the defined classes.
        Enumeration cmdNames = props.propertyNames();
        while (cmdNames.hasMoreElements())
        {
            String cmdName = (String) cmdNames.nextElement();
            String cmdClassName = props.getProperty(cmdName);
            try
            {
                Class clazz = Class.forName(cmdClassName);
                definitions.put(cmdName, clazz);
                factory.registerType(cmdName, clazz);

            } catch (ClassNotFoundException e)
            {
                LOG.warning("Could not locate " + cmdClassName + ". Command " + cmdName + " has been disabled.");
            }
        }
    }

    public Class getCommandDefinition(String name)
    {
        return commandDefinitions.get(name);
    }

    public Command createCommand(String name, ConfigContext context, Element element, CommandCommon common)
            throws ConfigException
    {
        return commandFactory.createCommand(name, context, element, common);
    }

    public Class getPostProcessorDefinition(String name)
    {
        return postProcessorDefinitions.get(name);
    }

    public PostProcessor createPostProcessor(String name, ConfigContext context, Element element, PostProcessorCommon common, Project project) 
            throws ConfigException
    {
        return postProcessorFactory.createPostProcessor(name, context, element, common, project);
    }
}
