package com.cinnamonbob.core2;

import com.cinnamonbob.core2.schedule.Trigger;
import com.cinnamonbob.core2.task.Task;
import com.cinnamonbob.core2.type.Type;
import nu.xom.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * 
 *
 */
public class ProjectLoader
{
    /**
     * Registered type definitions, used to interpret the project configuration.
     */ 
    private Map<String, Class> typeDefinitions = new HashMap<String, Class>();

    /**
     * Registered task definitions, used to interpret the project configuration.
     */ 
    private Map<String, Class> taskDefinitions = new HashMap<String, Class>();
    
    private Map<String, Class> triggerDefinitions = new HashMap<String, Class>();

    
    public ProjectLoader() throws Exception
    {
        initialise("/com/cinnamonbob/core2/type/defaults.properties", typeDefinitions);
        initialise("/com/cinnamonbob/core2/task/defaults.properties", taskDefinitions);
        initialise("/com/cinnamonbob/core2/schedule/defaults.properties", triggerDefinitions);
    }

    public Project load(File file)
            throws IOException, BobException
    {
        return load(new FileInputStream(file));
    }

    public Project load(String resource)
            throws IOException, BobException
    {
        return load(getClass().getResourceAsStream(resource));
    }

    public Project load(InputStream input)
            throws IOException, BobException
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

            Project project = new Project();
            setAttributes(rootElement, project, project);

            List<Element> recipes = new LinkedList<Element>();
            List<Element> schedules = new LinkedList<Element>();

            for (int i = 0; i < rootElement.getChildCount(); i++)
            {
                Node child = rootElement.getChild(i);
                if (child instanceof Element)
                {
                    Element e = (Element) child;
                    String typeName = e.getLocalName();

                    if (typeName.equals("recipe"))
                    {
                        recipes.add(e);
                    } else if (typeName.equals("schedule"))
                    {
                        schedules.add(e);
                    } else
                    {
                        loadType(e, project);
                    }
                }
            }

            // evaluate project variables
            project.resolveAllProperties();

            for (Element e : recipes)
            {
                loadRecipe(e, project);
            }

            for (Element e : schedules)
            {
                loadSchedule(e, project);
            }
            return project;
        } finally
        {
            if (input != null)
            {
                input.close();
            }
        }
    }

    private void loadSchedule(Element scheduleElement, Project project) throws BobException
    {
        Schedule schedule = new Schedule();
        
        setAttributes(scheduleElement, schedule, project);
        project.addSchedule(schedule);

        for (int j = 0; j < scheduleElement.getChildCount(); j++)
        {
            Node child = scheduleElement.getChild(j);
            if (child instanceof Element)
            {
                Element triggerElement = (Element) child;
                String triggerName = triggerElement.getLocalName();
                Class triggerClass = triggerDefinitions.get(triggerName);
                Trigger triggerInstance;
                try
                {
                    triggerInstance = (Trigger) triggerClass.newInstance();
                } catch (IllegalAccessException e)
                {
                    throw new ParseException("Failed to instantiate '" +
                            triggerName + "'", e, triggerElement);
                }
                catch (InstantiationException e)
                {
                    throw new ParseException("Failed to instantiate '" +
                            triggerName + "'", e, triggerElement);
                }

                setAttributes(triggerElement, triggerInstance, project);
                schedule.addTrigger(triggerInstance);

                // need to check for nesting.
                loadNested(triggerElement, project, triggerInstance);
            }
        }

    }

    private void loadRecipe(Element recipeElement, Project project)
            throws ParseException

    {
        Recipe recipe = new Recipe();
        setAttributes(recipeElement, recipe, project);
        project.addRecipe(recipe);

        for (int j = 0; j < recipeElement.getChildCount(); j++)
        {
            Node child = recipeElement.getChild(j);
            if (child instanceof Element)
            {
                Element taskElement = (Element) child;
                String taskName = taskElement.getLocalName();
                Class taskClass = taskDefinitions.get(taskName);
                Task taskInstance;
                try
                {
                    taskInstance = (Task) taskClass.newInstance();
                } catch (IllegalAccessException e)
                {
                    throw new ParseException("Failed to instantiate '" +
                            taskName + "'", e, taskElement);
                }
                catch (InstantiationException e)
                {
                    throw new ParseException("Failed to instantiate '" +
                            taskName + "'", e, taskElement);
                }

                setAttributes(taskElement, taskInstance, project);
                recipe.addTask(taskInstance);

                // need to check for nesting.
                loadNested(taskElement, project, taskInstance);
            }
        }
    }

    private void loadNested(Element e, Project project, Object parent)
            throws ParseException

    {
        IntrospectionHelper helper = IntrospectionHelper.getHelper(parent.getClass());
        for (int i = 0; i < e.getChildCount(); i++)
        {
            Node child = e.getChild(i);
            if (child instanceof Element)
            {
                Element nestedElement = (Element) child;
                try
                {
                    Object obj = helper.create(project, parent, nestedElement.getLocalName());
                    setAttributes(nestedElement, obj, project);
                    loadNested(nestedElement, project, obj);
                }
                catch (IllegalAccessException ex)
                {
                    throw new ParseException("Unable to setup nested '" +
                            nestedElement.getLocalName() + "'", ex, nestedElement);
                }
                catch (InvocationTargetException ex)
                {
                    throw new ParseException("Unable to setup nested '" +
                            nestedElement.getLocalName() + "'", ex, nestedElement);
                }
            }
        }
    }

    private void loadType(Element source, Project project)
            throws BobException

    {
        String typeName = source.getLocalName();
        if (!typeDefinitions.containsKey(typeName))
        {
            throw new ParseException("Unknown type '" + typeName + "' specified", source);
        }
        Class typeClass = typeDefinitions.get(typeName);
        Type typeInstance;
        try
        {
            typeInstance = (Type) typeClass.newInstance();
        } catch (IllegalAccessException e)
        {
            throw new ParseException("Failed to instantiate type " +
                    typeName, e, source);
        } catch (InstantiationException e)
        {
            throw new ParseException("Failed to instantiate type " +
                    typeName, e, source);
        }

        setAttributes(source, typeInstance, project);
        typeInstance.setProject(project);

        // support adding text is supported by type.
        IntrospectionHelper helper = IntrospectionHelper.getHelper(typeClass);
        if (helper.canAddText())
        {
            for (int j = 0; j < source.getChildCount(); j++)
            {
                Node grandChild = source.getChild(j);
                if (grandChild instanceof Text)
                {
                    Text e2 = (Text) grandChild;
                    try
                    {
                        helper.addText(typeInstance, e2.getValue());
                    } catch (IllegalAccessException e)
                    {
                        throw new ParseException("Failed to addText to " +
                                typeName, e, source);
                    } catch (InvocationTargetException e)
                    {
                        throw new ParseException("Failed to addText to " +
                                typeName, e, source);
                    }
                }
            }
        }

        typeInstance.execute();
        if (typeInstance.getId() != null)
        {
            project.addReference(typeInstance.getId(), typeInstance);
        }
    }

    private String extractLocationString(Element e)
    {
        if (e instanceof LocationAwareElement)
        {
            LocationAwareElement lae = (LocationAwareElement) e;
            return " at line " + lae.getLineNumber() + ", column " + lae.getColumnNumber() + ".";
        }
        return ".";
    }
    
    /**
     * @param source
     * @param target
     * @param p
     */
    private void setAttributes(Element source, Object target, Project p)
            throws ParseException
    {
        IntrospectionHelper helper = IntrospectionHelper.getHelper(target.getClass());

        try
        {
            String resolvedAttributeValue;
            for (int i = 0; i < source.getAttributeCount(); i++)
            {
                Attribute a = source.getAttribute(i);
                resolvedAttributeValue = p.resolveProperties(a.getValue());
                try
                {
                    helper.set(p, target, a.getLocalName(), resolvedAttributeValue);
                }
                catch (IllegalAccessException e)
                {
                    throw new ParseException("Unable to set attribute data '" +
                            resolvedAttributeValue + "' for '" + a.getLocalName() +
                            "'" + " on " + source.getLocalName() + "'", e, source);
                }
                catch (InvocationTargetException e)
                {
                    throw new ParseException("Unable to set attribute data '" +
                            resolvedAttributeValue + "' for '" + a.getLocalName() +
                            "'" + " on " + source.getLocalName() + "'", e, source);
                }
            }
        }
        catch (ParseException e)
        {
            throw e;
        }
        catch (BobException e)
        {
            throw new ParseException(e.getMessage(), e, source);
        }
    }

    /**
     * Register a new type definition.
     * 
     * @param name
     * @param type
     */ 
    public void registerTypeDefinition(String name, Class type)
    {
        if (typeDefinitions.containsKey(name))
        {
            throw new IllegalArgumentException("You can not change an existing type definition.");
        }
        typeDefinitions.put(name, type);
    }

    /**
     * Register a new task definition
     * 
     * @param name
     * @param task
     */ 
    public void registerTaskDefinition(String name, Class task)
    {
        if (taskDefinitions.containsKey(name))
        {
            throw new IllegalArgumentException("You can not change an existing task definition.");
        }
        taskDefinitions.put(name, task);
    }
    
    private void initialise(String resourceName, Map<String, Class> store)
            throws IOException, ClassNotFoundException
    {
        Properties types = loadProperties(resourceName);
        Enumeration names = types.propertyNames();
        while (names.hasMoreElements())
        {
            String name = (String) names.nextElement();
            String className = types.getProperty(name);
            Class clz = Class.forName(className);
            store.put(name, clz);
        }
    }

    //---(To be extracted into a Util class)---
    private Properties loadProperties(String resource) throws IOException
    {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream(resource));
        return props;
    }


}
