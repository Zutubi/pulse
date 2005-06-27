package com.cinnamonbob.core2.config;

import com.cinnamonbob.BobException;
import com.cinnamonbob.util.IOHelper;
import nu.xom.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 */
public class ProjectConfigurationLoader
{
    private final Map<String, Class> typeDefinitions = new HashMap<String, Class>();

    private Project project = null;

    private List<InitComponent> requireInitialisation = new LinkedList<InitComponent>();

    public ProjectConfigurationLoader()
    {
    }

//    private Map<String, Class> loadConfig(String resourceName)
//            throws IOException
//    {
//        Map<String, Class> data = new HashMap<String, Class>();
//        InputStream resource = getClass().getResourceAsStream(resourceName);
//        if (resource == null)
//        {
//            // could not locate config file... skip.
//            return new HashMap<String, Class>(0);
//        }
//        Properties types = IOHelper.read(resource);
//        Enumeration names = types.propertyNames();
//        while (names.hasMoreElements())
//        {
//            try
//            {
//                String name = (String) names.nextElement();
//                String className = types.getProperty(name);
//                Class clz = Class.forName(className);
//                data.put(name, clz);
//            }
//            catch (ClassNotFoundException e)
//            {
//                // unknown class referenced in properties file, skip.
//                e.printStackTrace();
//            }
//        }
//        return data;
//    }

    public Project load(File file) throws BobException, IOException, IllegalAccessException, InvocationTargetException
    {
        return load(new FileInputStream(file));
    }

    public Project load(InputStream input) throws BobException, IOException, IllegalAccessException, InvocationTargetException
    {
        try
        {
            Builder builder = new Builder(new LocationAwareNodeFactory());

            Document doc;
            try
            {
                doc = builder.build(input);
            }
            catch (nu.xom.ParsingException pex)
            {
                throw new ParseException(pex);
            }

            Element rootElement = doc.getRootElement();
            project = new Project();

            //TODO: FIX THIS.
            project.setProperty("work.dir", ".");

            // brief bootstraping of the loading process - can not treat project like a type since
            // it does not have a parent... it is the root afterall.
            mapAttributesToProperties(rootElement, project);

            for (int index = 0; index < rootElement.getChildCount(); index++)
            {
                Node node = rootElement.getChild(index);
                if (!(node instanceof Element))
                {
                    continue;
                }
                loadType((Element) node, project);
            }

            return project;
        }
        finally
        {
            IOHelper.close(input);
        }
    }

    private void loadType(Element e, Object parent) throws BobException, IllegalAccessException, InvocationTargetException
    {
        IntrospectionHelper parentHelper = IntrospectionHelper.getHelper(parent.getClass());
        String name = e.getLocalName();

        Object type;

        // if create factory method, use it
        String propertyName = convertLocalNameToPropertyName(name);
        if (parentHelper.hasCreate(propertyName))
        {
            // we expect this type to have been added to its parent during creation.
            type = parentHelper.create(propertyName, parent);
        } else
        {
            // this one we still need to add to its parent when its initialisation is completed.
            type = create(name);
        }

        IntrospectionHelper typeHelper = IntrospectionHelper.getHelper(type.getClass());

        // initialise attributes
        mapAttributesToProperties(e, type);

        // interface based initialisation.
        if (Reference.class.isAssignableFrom(type.getClass()))
        {
            String referenceName = ((Reference) type).getName();
            if (referenceName != null && referenceName.length() > 0)
            {
                project.setReference(referenceName, (Reference) type);
            }
        }

        if (ProjectComponent.class.isAssignableFrom(type.getClass()))
        {
            ((ProjectComponent) type).setProject(project);
        }

        // initialise sub-elements.
        for (int index = 0; index < e.getChildCount(); index++)
        {
            Node node = e.getChild(index);

            if (node instanceof Element)
            {
                Element element = (Element) node;
                // process type.
                loadType(element, type);

            } else if (node instanceof Text)
            {
                if (typeHelper.hasAddText())
                {
                    typeHelper.addText(type, node.getValue());
                }
            }
        }

        // add to container.
        if (parentHelper.hasAdd(propertyName))
        {
            parentHelper.add(propertyName, parent, type);
        } else if (parentHelper.canAdd(type.getClass()))
        {
            parentHelper.add(parent, type);
        }

        if (InitComponent.class.isAssignableFrom(type.getClass()))
        {
            ((InitComponent) type).init();
        }
    }

    private Object create(String name) throws ParseException
    {
        Class clz = typeDefinitions.get(name);
        if (clz != null)
        {
            try
            {
                return clz.newInstance();
            } catch (Exception e)
            {
                throw new ParseException("Could not instantiate type '" + name + "'");
            }
        }
        throw new ParseException("Undefined type '" + name + "'");
    }

    /**
     * Simple helper that converts some-name to someName. ie: remove the '-' and
     * upper case the following letter.
     *
     * @param name
     * @return
     */
    private String convertLocalNameToPropertyName(String name)
    {
        while (name.indexOf('-') != -1)
        {
            int index = name.indexOf('-');
            name = name.substring(0, index) + name.substring(index + 1, index + 2).toUpperCase() + name.substring(index + 2);
        }
        return name;
    }

    private void mapAttributesToProperties(Element source, Object target) throws ParseException
    {
        IntrospectionHelper helper = IntrospectionHelper.getHelper(target.getClass());

        for (int i = 0; i < source.getAttributeCount(); i++)
        {
            Attribute a = source.getAttribute(i);

            try
            {
                String propertyName = convertLocalNameToPropertyName(a.getLocalName());
                helper.set(propertyName, target, project.replaceVariables(a.getValue()), project);
            }
            catch (Exception e)
            {
                throw new ParseException(e);
            }
        }
    }

    public void register(String name, Class type)
    {
        typeDefinitions.put(name, type);
    }
}
