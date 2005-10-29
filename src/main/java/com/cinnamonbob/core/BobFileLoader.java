package com.cinnamonbob.core;

import com.cinnamonbob.BobException;
import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.core.validation.CommandValidationManager;
import com.cinnamonbob.core.validation.CommandValidationException;
import com.cinnamonbob.util.IOUtils;
import com.opensymphony.xwork.spring.SpringObjectFactory;
import com.opensymphony.xwork.validator.ValidationException;
import nu.xom.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 */
public class BobFileLoader
{
    private final Map<String, Class> typeDefinitions = new HashMap<String, Class>();

    private BobFile bobFile = null;

    private SpringObjectFactory springFactory;

    public BobFileLoader()
    {
    }

    private SpringObjectFactory getSpringFactory()
    {
        if (springFactory == null)
        {
            springFactory = new SpringObjectFactory();
            springFactory.setApplicationContext(ComponentContext.getContext());
        }
        return springFactory;
    }

    public BobFile load(File file) throws BobException, IOException, IllegalAccessException, InvocationTargetException
    {
        return load(new FileInputStream(file));
    }

    public BobFile load(InputStream input) throws BobException, IOException, IllegalAccessException, InvocationTargetException
    {
        return load(input, null);
    }

    public BobFile load(InputStream input, Map<String, String> properties) throws BobException, IOException, IllegalAccessException, InvocationTargetException
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
            bobFile = new BobFile();
            
            if(properties != null)
            {
                bobFile.addProperties(properties);
            }

            // brief bootstraping of the loading process - can not treat bobfile like a type since
            // it does not have a parent... it is the root afterall.
            mapAttributesToProperties(rootElement, bobFile);

            for (int index = 0; index < rootElement.getChildCount(); index++)
            {
                Node node = rootElement.getChild(index);
                if (!(node instanceof Element))
                {
                    continue;
                }
                loadType((Element) node, bobFile);
            }

            return bobFile;
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    private void loadType(Element e, Object parent) throws BobException, IllegalAccessException
    {
        IntrospectionHelper parentHelper = IntrospectionHelper.getHelper(parent.getClass(), typeDefinitions);
        String name = e.getLocalName();

        Object type;

        try
        {
            // if create factory method, use it
            String propertyName = convertLocalNameToPropertyName(name);
            if (parentHelper.hasCreate(propertyName))
            {
                // we expect this type to have been added to its parent during creation.
                type = parentHelper.create(propertyName, parent);
            }
            else
            {
                // this one we still need to add to its parent when its initialisation is completed.
                type = create(name);
            }

            // autowire the object using the springs autowire. This provides full access to the systems
            // resources from within the type instances.
            getSpringFactory().autoWireBean(type);

            IntrospectionHelper typeHelper = IntrospectionHelper.getHelper(type.getClass(), typeDefinitions);

            // initialise attributes
            mapAttributesToProperties(e, type);

            // interface based initialisation.
            if (Reference.class.isAssignableFrom(type.getClass()))
            {
                String referenceName = ((Reference) type).getName();
                if (referenceName != null && referenceName.length() > 0)
                {
                    if (bobFile.getReference(referenceName) != null)
                    {
                        throw new FileLoadException("An entity named '" + referenceName + "' already exists.");
                    }

                    bobFile.setReference(referenceName, (Reference) type);
                }
            }

            if (BobFileComponent.class.isAssignableFrom(type.getClass()))
            {
                ((BobFileComponent) type).setBobFile(bobFile);
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

                }
                else if (node instanceof Text)
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
            }
            else if (parentHelper.canAdd(type.getClass()))
            {
                parentHelper.add(parent, type);
            }

            if (InitComponent.class.isAssignableFrom(type.getClass()))
            {
                ((InitComponent) type).init();
            }

            // Apply declarative validation
            CommandValidationManager.validate(type, name);
        }
        catch (InvocationTargetException ex)
        {
            Throwable t;

            if (ex.getCause() != null)
            {
                t = ex.getCause();
            }
            else
            {
                t = ex;
            }

            throw new ParseException(createParseErrorMessage(name, e, t));
        }
        catch (ValidationException ex)
        {
            throw new ParseException(createParseErrorMessage(name, e, ex));
        }
        catch (FileLoadException ex)
        {
            throw new ParseException(createParseErrorMessage(name, e, ex));
        }
    }

    private String createParseErrorMessage(String name, Element element, Throwable t)
    {
        StringBuilder message = new StringBuilder(256);

        message.append("Processing element '");
        message.append(name);
        message.append("': ");

        if(element instanceof LocationAwareElement)
        {
            LocationAwareElement location = (LocationAwareElement)element;
            message.append("starting at line ");
            message.append(location.getLineNumber());
            message.append(" column ");
            message.append(location.getColumnNumber());
            message.append(": ");
        }

        message.append(t.getMessage());
        return message.toString();
    }

    private Object create(String name) throws FileLoadException
    {
        Class clz = typeDefinitions.get(name);
        if (clz != null)
        {
            try
            {
                return clz.newInstance();
            } catch (Exception e)
            {
                throw new FileLoadException("Could not instantiate type '" + name + "'");
            }
        }
        throw new FileLoadException("Undefined type '" + name + "'");
    }

    /**
     * Simple helper that converts some-name to someName. ie: remove the '-' and
     * upper case the following letter.
     *
     * @param name
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

    private void mapAttributesToProperties(Element source, Object target) throws FileLoadException
    {
        IntrospectionHelper helper = IntrospectionHelper.getHelper(target.getClass(), typeDefinitions);

        for (int i = 0; i < source.getAttributeCount(); i++)
        {
            Attribute a = source.getAttribute(i);

            try
            {
                String propertyName = convertLocalNameToPropertyName(a.getLocalName());
                helper.set(propertyName, target, bobFile.replaceVariables(a.getValue()), bobFile);
            }
            catch (Exception e)
            {
                throw new FileLoadException(e.getMessage());
            }
        }
    }

    public void register(String name, Class type)
    {
        typeDefinitions.put(name, type);
    }
}
