package com.zutubi.pulse.core;

import com.zutubi.pulse.core.validation.CommandValidationManager;
import com.zutubi.pulse.util.IOUtils;
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
public class FileLoader
{
    private static final int MAX_RECURSION_DEPTH = 128;

    private final Map<String, Class> typeDefinitions = new HashMap<String, Class>();
    private ObjectFactory factory;

    public FileLoader()
    {
        // For the Spring
    }

    public FileLoader(ObjectFactory factory)
    {
        setObjectFactory(factory);
    }

    /**
     * The object factory will be used to instantiate new instances of types.
     *
     * @param factory
     */
    public void setObjectFactory(ObjectFactory factory)
    {
        this.factory = factory;
    }

    public void load(File file, Object root) throws PulseException, IOException, IllegalAccessException, InvocationTargetException
    {
        load(new FileInputStream(file), root);
    }

    public void load(InputStream input, Object root) throws PulseException
    {
        load(input, root, null, new FileResourceRepository(), null);
    }

    public void load(InputStream input, Object root, Scope globalScope, ResourceRepository resourceRepository, TypeLoadPredicate predicate) throws PulseException
    {
        try
        {
            Builder builder = new Builder(new LocationAwareNodeFactory());

            Document doc;
            try
            {
                doc = builder.build(input);
            }
            catch (ParsingException pex)
            {
                throw new ParseException(pex);
            }
            catch (IOException e)
            {
                throw new ParseException(e);
            }

            if (globalScope == null)
            {
                globalScope = new Scope();
            }

            // brief bootstraping of the loading process
            Element rootElement = doc.getRootElement();

            if (ScopeAware.class.isAssignableFrom(root.getClass()))
            {
                ((ScopeAware) root).setScope(globalScope);
            }

            mapAttributesToProperties(rootElement, root, true, globalScope);

            for (int index = 0; index < rootElement.getChildCount(); index++)
            {
                Node node = rootElement.getChild(index);
                if (!(node instanceof Element))
                {
                    continue;
                }
                loadType((Element) node, root, true, globalScope, 1, resourceRepository, predicate);
            }
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    private void loadType(Element e, Object parent, boolean resolveReferences, Scope scope, int depth, ResourceRepository resourceRepository, TypeLoadPredicate predicate) throws PulseException
    {
        IntrospectionHelper parentHelper = IntrospectionHelper.getHelper(parent.getClass(), typeDefinitions);
        String name = e.getLocalName();

        Object type;

        try
        {
            if(depth > MAX_RECURSION_DEPTH)
            {
                throw new FileLoadException("Maximum recursion depth exceeded");
            }

            if(handleInternalElement(e, parent, resolveReferences, scope, parentHelper, depth, resourceRepository, predicate))
            {
                return;
            }

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

            if(predicate != null)
            {
                resolveReferences = resolveReferences && predicate.resolveReferences(type, e);
            }

            IntrospectionHelper typeHelper = IntrospectionHelper.getHelper(type.getClass(), typeDefinitions);

            // initialise attributes
            mapAttributesToProperties(e, type, resolveReferences, scope);

            // interface based initialisation.
            if (Reference.class.isAssignableFrom(type.getClass()))
            {
                String referenceName = ((Reference) type).getName();
                if (referenceName != null && referenceName.length() > 0)
                {
                    scope.setReference((Reference) type);
                }
            }

            boolean loadType = predicate == null || predicate.loadType(type, e);
            if(loadType)
            {
                scope = new Scope(scope);

                if (ScopeAware.class.isAssignableFrom(type.getClass()))
                {
                    ((ScopeAware) type).setScope(scope);
                }

                if (ResourceAware.class.isAssignableFrom(type.getClass()))
                {
                    ((ResourceAware) type).setResourceRepository(resourceRepository);
                }

                if (InitComponent.class.isAssignableFrom(type.getClass()))
                {
                    ((InitComponent) type).initBeforeChildren();
                }

                // initialise sub-elements.
                loadSubElements(e, type, resolveReferences, scope, typeHelper, depth, resourceRepository, predicate);
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

            if(loadType)
            {
                if (InitComponent.class.isAssignableFrom(type.getClass()))
                {
                    ((InitComponent) type).initAfterChildren();
                }

                // Apply declarative validation
                CommandValidationManager.validate(type, name);
            }
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

            throw createParseException(name, e, t);
        }
        catch (ParseException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw createParseException(name, e, ex);
        }
    }

    private void loadSubElements(Element e, Object type, boolean resolveReferences, Scope scope, IntrospectionHelper typeHelper, int depth, ResourceRepository resourceRepository, TypeLoadPredicate predicate)
            throws Exception
    {
        for (int index = 0; index < e.getChildCount(); index++)
        {
            Node node = e.getChild(index);

            if (node instanceof Element)
            {
                Element element = (Element) node;
                // process type.
                loadType(element, type, resolveReferences, scope, depth + 1, resourceRepository, predicate);
            }
            else if (node instanceof Text)
            {
                if (typeHelper.hasAddText())
                {
                    typeHelper.addText(type, node.getValue());
                }
            }
        }
    }

    private boolean handleInternalElement(Element element, Object type, boolean resolveReferences, Scope scope, IntrospectionHelper typeHelper, int depth, ResourceRepository resourceRepository, TypeLoadPredicate predicate) throws Exception
    {
        String localName = element.getLocalName();
        if(localName.equals("macro"))
        {
            // Macro definition, get name and store child elements
            boolean found = false;

            for(int i = 0; i < element.getAttributeCount(); i++)
            {
                Attribute attribute = element.getAttribute(i);
                if(attribute.getLocalName().equals("name"))
                {
                    scope.setReference(new Macro(attribute.getValue(), element));
                    found = true;
                }
                else
                {
                    throw new FileLoadException("Unrecognised attribute '" + attribute.getLocalName() + "'");
                }
            }

            if(!found)
            {
                throw new FileLoadException("Required attribute 'name' not found");
            }

            return true;
        }
        else if(localName.equals("macro-ref"))
        {
            // Macro referece.  Lookup macro, and load all it's children now.
            boolean found = false;

            for(int i = 0; i < element.getAttributeCount(); i++)
            {
                Attribute attribute = element.getAttribute(i);
                if(attribute.getLocalName().equals("macro"))
                {
                    String macroName = attribute.getValue();

                    Object o = VariableHelper.replaceVariable(macroName, scope);
                    if(!LocationAwareElement.class.isAssignableFrom(o.getClass()))
                    {
                        throw new FileLoadException("Reference '" + macroName + "' does not resolve to a macro");
                    }

                    LocationAwareElement lae = (LocationAwareElement) o;

                    try
                    {
                        loadSubElements(lae, type, resolveReferences, scope, typeHelper, depth, resourceRepository, predicate);
                    }
                    catch(Exception e)
                    {
                        throw new FileLoadException("While expanding macro defined at line " + lae.getLineNumber() + " column " + lae.getColumnNumber() + ": " + e.getMessage(), e);
                    }
                    found = true;
                }
                else
                {
                    throw new FileLoadException("Unrecognised attribute '" + attribute.getLocalName() + "'");
                }
            }

            if(!found)
            {
                throw new FileLoadException("Required attribute 'macro' not found");
            }

            return true;
        }

        return false;
    }

    private ParseException createParseException(String name, Element element, Throwable t)
    {
        int line = -1;
        int column = -1;

        StringBuilder message = new StringBuilder(256);

        message.append("Processing element '");
        message.append(name);
        message.append("': ");

        if (element instanceof LocationAwareElement)
        {
            LocationAwareElement location = (LocationAwareElement) element;
            message.append("starting at line ");
            line = location.getLineNumber();
            message.append(line);
            message.append(" column ");
            column = location.getColumnNumber();
            message.append(column);
            message.append(": ");
        }

        message.append(t.getMessage());
        return new ParseException(line, column, message.toString());
    }

    private Object create(String name) throws FileLoadException
    {
        Class clz = typeDefinitions.get(name);
        if (clz != null)
        {
            try
            {
                return factory.buildBean(clz);
            }
            catch (Exception e)
            {
                throw new FileLoadException("Could not instantiate type '" + name + "'. Reason: " + e.getMessage());
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

    private void mapAttributesToProperties(Element source, Object target, boolean resolveReferences, Scope scope) throws FileLoadException
    {
        IntrospectionHelper helper = IntrospectionHelper.getHelper(target.getClass(), typeDefinitions);

        for (int i = 0; i < source.getAttributeCount(); i++)
        {
            Attribute a = source.getAttribute(i);

            try
            {
                String propertyName = convertLocalNameToPropertyName(a.getLocalName());
                helper.set(propertyName, target, a.getValue(), resolveReferences, scope);
            }
            catch (InvocationTargetException e)
            {
                throw new FileLoadException(e.getCause().getMessage());
            }
            catch (UnknownAttributeException e)
            {
                throw new FileLoadException("Unrecognised attribute '" + a.getLocalName() + "'.");
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
