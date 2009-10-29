package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.FileLoadException;
import com.zutubi.pulse.core.engine.api.Reference;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.pulse.core.engine.api.ScopeAware;
import com.zutubi.pulse.core.validation.CommandValidationException;
import com.zutubi.pulse.core.validation.PulseValidationContext;
import com.zutubi.pulse.core.validation.PulseValidationManager;
import com.zutubi.util.TextUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.io.IOUtils;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;
import com.zutubi.validation.i18n.MessagesTextProvider;
import nu.xom.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class FileLoader
{
    private static final int MAX_RECURSION_DEPTH = 128;

    private final Map<String, Class> typeDefinitions = new HashMap<String, Class>();
    private ObjectFactory factory;

    private ValidationManager validationManager = new PulseValidationManager();

    public FileLoader()
    {
        // For the Spring
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    /**
     * The object factory will be used to instantiate new instances of types.
     *
     * @param factory instance
     */
    public void setObjectFactory(ObjectFactory factory)
    {
        this.factory = factory;
    }

    public void load(File file, Object root, FileResolver fileResolver) throws PulseException, IOException
    {
        load(new FileInputStream(file), root, fileResolver);
    }

    public void load(InputStream input, Object root, FileResolver fileResolver) throws PulseException
    {
        load(input, root, null, fileResolver, new FileResourceRepository(), null);
    }

    public void load(InputStream input, Object root, Scope globalScope, FileResolver fileResolver, ResourceRepository resourceRepository, TypeLoadPredicate predicate) throws PulseException
    {
        if (predicate == null)
        {
            predicate = new DefaultTypeLoadPredicate();
        }

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
                throw new ParseException(pex.getLineNumber(), pex.getColumnNumber(), pex);
            }
            catch (IOException e)
            {
                throw new ParseException(e);
            }

            if (globalScope == null)
            {
                globalScope = new PulseScope();
            }

            // brief bootstraping of the loading process
            Element rootElement = doc.getRootElement();

            if (ScopeAware.class.isAssignableFrom(root.getClass()))
            {
                ((ScopeAware) root).setScope(globalScope);
            }

            mapAttributesToProperties(rootElement, root, predicate, globalScope);

            for (int index = 0; index < rootElement.getChildCount(); index++)
            {
                Node node = rootElement.getChild(index);
                if (!(node instanceof Element))
                {
                    continue;
                }
                loadType((Element) node, root, globalScope, 1, fileResolver, resourceRepository, predicate);
            }
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    private void loadType(Element e, Object parent, Scope scope, int depth, FileResolver fileResolver, ResourceRepository resourceRepository, TypeLoadPredicate predicate) throws PulseException
    {
        IntrospectionHelper parentHelper = IntrospectionHelper.getHelper(parent.getClass(), typeDefinitions);
        String name = e.getLocalName();

        Object type;

        try
        {
            if (depth > MAX_RECURSION_DEPTH)
            {
                throw new FileLoadException(String.format("Maximum recursion depth %s exceeded", MAX_RECURSION_DEPTH));
            }

            if (handleInternalElement(e, parent, scope, parentHelper, depth, fileResolver, resourceRepository, predicate))
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

            IntrospectionHelper typeHelper = IntrospectionHelper.getHelper(type.getClass(), typeDefinitions);

            // initialise attributes
            mapAttributesToProperties(e, type, predicate, scope);

            // interface based initialisation.
            if (Reference.class.isAssignableFrom(type.getClass()))
            {
                String referenceName = ((Reference) type).getName();
                if (referenceName != null && referenceName.length() > 0)
                {
                    scope.addUnique((Reference) type);
                }
            }

            boolean loadType = predicate.loadType(type, e);
            if (loadType)
            {
                scope = scope.createChild();

                if (ScopeAware.class.isAssignableFrom(type.getClass()))
                {
                    ((ScopeAware) type).setScope(scope);
                }

                if (ResourceAware.class.isAssignableFrom(type.getClass()))
                {
                    ((ResourceAware) type).setResourceRepository(resourceRepository);
                }

                if (FileLoaderAware.class.isAssignableFrom(type.getClass()))
                {
                    ((FileLoaderAware) type).setFileLoader(this);
                }

                if (InitComponent.class.isAssignableFrom(type.getClass()))
                {
                    ((InitComponent) type).initBeforeChildren();
                }

                // initialise sub-elements.
                loadSubElements(e, type, scope, typeHelper, depth, fileResolver, resourceRepository, predicate);
            }

            // add to container.
            if (parentHelper.hasAdd(propertyName))
            {
                parentHelper.add(propertyName, parent, type, scope);
            }
            else if (parentHelper.canAdd(type.getClass()))
            {
                parentHelper.add(parent, type, scope);
            }

            if (loadType)
            {
                if (InitComponent.class.isAssignableFrom(type.getClass()))
                {
                    ((InitComponent) type).initAfterChildren();
                }
            }

            // Apply declarative validation
            if (predicate.validate(type, e))
            {
                validate(type);
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

    private void validate(Object obj) throws CommandValidationException, ValidationException
    {
        ValidationContext validationContext = new PulseValidationContext(new MessagesTextProvider(obj));
        validationManager.validate(obj, validationContext);
        if (validationContext.hasErrors())
        {
            throw new CommandValidationException(validationContext);
        }
    }

    private void loadSubElements(Element e, Object type, Scope scope, IntrospectionHelper typeHelper, int depth, FileResolver fileResolver, ResourceRepository resourceRepository, TypeLoadPredicate predicate)
            throws Exception
    {
        String text = null;

        for (int index = 0; index < e.getChildCount(); index++)
        {
            Node node = e.getChild(index);

            if (node instanceof Element)
            {
                Element element = (Element) node;
                // process type.
                loadType(element, type, scope, depth + 1, fileResolver, resourceRepository, predicate);
            }
            else if (node instanceof Text)
            {
                if (text == null)
                {
                    text = node.getValue();
                }
                else
                {
                    text += node.getValue();
                }
            }
        }

        if (text != null && typeHelper.hasSetText())
        {
            ReferenceResolver.ResolutionStrategy resolutionStrategy = getResolutionStrategy(predicate, type, e);

            text = ReferenceResolver.resolveReferences(text, scope, resolutionStrategy);
            typeHelper.setText(type, text);
        }
    }

    private ReferenceResolver.ResolutionStrategy getResolutionStrategy(TypeLoadPredicate predicate, Object type, Element e)
    {
        ReferenceResolver.ResolutionStrategy resolutionStrategy = ReferenceResolver.ResolutionStrategy.RESOLVE_NONE;
        if (predicate.resolveReferences(type, e))
        {
            if (predicate.allowUnresolved(type, e))
            {
                resolutionStrategy = ReferenceResolver.ResolutionStrategy.RESOLVE_NON_STRICT;
            }
            else
            {
                resolutionStrategy = ReferenceResolver.ResolutionStrategy.RESOLVE_STRICT;
            }
        }
        return resolutionStrategy;
    }

    private boolean handleInternalElement(Element element, Object type, Scope scope, IntrospectionHelper typeHelper, int depth, FileResolver fileResolver, ResourceRepository resourceRepository, TypeLoadPredicate predicate) throws Exception
    {
        String localName = element.getLocalName();
        if (localName.equals("macro"))
        {
            // Macro definition, get name and store child elements
            boolean found = false;

            for (int i = 0; i < element.getAttributeCount(); i++)
            {
                Attribute attribute = element.getAttribute(i);
                if (attribute.getLocalName().equals("name"))
                {
                    scope.addUnique(new Macro(attribute.getValue(), element));
                    found = true;
                }
                else
                {
                    throw new FileLoadException("Unrecognised attribute '" + attribute.getLocalName() + "'");
                }
            }

            if (!found)
            {
                throw new FileLoadException("Required attribute 'name' not found");
            }

            return true;
        }
        else if (localName.equals("macro-ref"))
        {
            // Macro referece.  Lookup macro, and load all it's children now.
            boolean found = false;

            for (int i = 0; i < element.getAttributeCount(); i++)
            {
                Attribute attribute = element.getAttribute(i);
                if (attribute.getLocalName().equals("macro"))
                {
                    String macroName = attribute.getValue();

                    Object o = ReferenceResolver.resolveReference(macroName, scope);
                    if (!LocationAwareElement.class.isAssignableFrom(o.getClass()))
                    {
                        throw new FileLoadException("Reference '" + macroName + "' does not resolve to a macro");
                    }

                    LocationAwareElement lae = (LocationAwareElement) o;

                    try
                    {
                        loadSubElements(lae, type, scope, typeHelper, depth, fileResolver, resourceRepository, predicate);
                    }
                    catch (Exception e)
                    {
                        throw new FileLoadException("\nWhile expanding macro defined at line " + lae.getLineNumber() + " column " + lae.getColumnNumber() + ": " + e.getMessage(), e);
                    }
                    found = true;
                }
                else
                {
                    throw new FileLoadException("Unrecognised attribute '" + attribute.getLocalName() + "'");
                }
            }

            if (!found)
            {
                throw new FileLoadException("Required attribute 'macro' not found");
            }

            return true;
        }
        else if (localName.equals("scope"))
        {
            // Just load children in new scope and redirect to parent
            loadSubElements(element, type, scope.createChild(), typeHelper, depth, fileResolver, resourceRepository, predicate);
            return true;
        }
        else if (localName.equals("import"))
        {
            boolean optional = false;
            String path = null;
            for (int i = 0; i < element.getAttributeCount(); i++)
            {
                Attribute attribute = element.getAttribute(i);
                if (attribute.getLocalName().equals("path"))
                {
                    path = attribute.getValue();
                }
                else if (attribute.getLocalName().equals("optional"))
                {
                    optional = Boolean.valueOf(attribute.getValue());
                }
                else
                {
                    throw new FileLoadException("Unrecognised attribute '" + attribute.getLocalName() + "'");
                }
            }

            if (!TextUtils.stringSet(path))
            {
                throw new FileLoadException("Required attribute 'path' not set");
            }


            InputStream input;
            if (optional)
            {
                try
                {
                    input = fileResolver.resolve(path);
                }
                catch(Exception e)
                {
                    // Ignore for optional includes.
                    return true;
                }
            }
            else
            {
                input = fileResolver.resolve(path);
            }
            
            try
            {
                Document doc;
                try
                {
                    Builder builder = new Builder(new LocationAwareNodeFactory());
                    doc = builder.build(input);
                }
                finally
                {
                    IOUtils.close(input);
                }

                loadSubElements(doc.getRootElement(), type, scope, typeHelper, depth, new RelativeFileResolver(path, fileResolver), resourceRepository, predicate);
                return true;
            }
            catch (Exception e)
            {
                throw new FileLoadException("While importing file '" + path + "': " + e.getMessage(), e);
            }
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

        ParseException parseException = new ParseException(line, column, message.toString());
        parseException.initCause(t);
        return parseException;
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
     * @param name name to be converted
     * @return converted name
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

    private void mapAttributesToProperties(Element source, Object target, TypeLoadPredicate predicate, Scope scope) throws FileLoadException
    {
        IntrospectionHelper helper = IntrospectionHelper.getHelper(target.getClass(), typeDefinitions);

        for (int i = 0; i < source.getAttributeCount(); i++)
        {
            Attribute a = source.getAttribute(i);

            try
            {
                String propertyName = convertLocalNameToPropertyName(a.getLocalName());
                helper.set(propertyName, target, a.getValue(), getResolutionStrategy(predicate, target, source), scope);
            }
            catch (InvocationTargetException e)
            {
                throw new FileLoadException(e.getCause().getMessage(), e.getCause());
            }
            catch (UnknownAttributeException e)
            {
                throw new FileLoadException("Unrecognised attribute '" + a.getLocalName() + "'.", e);
            }
            catch (Exception e)
            {
                throw new FileLoadException(e.getMessage(), e);
            }
        }
    }

    public void register(String name, Class type)
    {
        typeDefinitions.put(name, type);
    }

    public boolean registered(String name)
    {
        return typeDefinitions.containsKey(name);
    }
}
