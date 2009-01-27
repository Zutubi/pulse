package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.FileLoadException;
import com.zutubi.pulse.core.engine.api.Reference;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.pulse.core.validation.CommandValidationException;
import com.zutubi.pulse.core.validation.PulseValidationContext;
import com.zutubi.pulse.core.validation.PulseValidationManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.squeezer.Squeezers;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.tove.type.*;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class ToveFileLoader
{
    private static final int MAX_RECURSION_DEPTH = 128;

    private final Map<String, CompositeType> typeDefinitions = new HashMap<String, CompositeType>();

    private ObjectFactory factory;
    private TypeRegistry typeRegistry;
    private ValidationManager validationManager = new PulseValidationManager();

    public ToveFileLoader()
    {
        // For the Spring
    }

    public CompositeType lookupType(Class clazz) throws FileLoadException
    {
        CompositeType type = typeRegistry.getType(clazz);
        if (type == null)
        {
            throw new FileLoadException("Unable to find type for class '" + clazz.getName() + "'");
        }

        return type;
    }

    public void load(File file, Configuration root) throws PulseException, IOException, IllegalAccessException, InvocationTargetException
    {
        load(new FileInputStream(file), root);
    }

    public void load(InputStream input, Configuration root) throws PulseException
    {
        load(input, root, null, null);
    }

    public void load(InputStream input, Configuration root, Scope globalScope, TypeLoadPredicate predicate) throws PulseException
    {
        if (predicate == null)
        {
            predicate = new DefaultTypeLoadPredicate();
        }

        CompositeType type = lookupType(root.getClass());
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

            mapAttributesToProperties(rootElement, root, type, predicate, globalScope);

            for (int index = 0; index < rootElement.getChildCount(); index++)
            {
                Node node = rootElement.getChild(index);
                if (!(node instanceof Element))
                {
                    continue;
                }
                loadType((Element) node, root, type, globalScope, 1, predicate);
            }
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    private void loadType(Element e, Configuration parent, CompositeType parentType, Scope scope, int depth, TypeLoadPredicate predicate) throws PulseException
    {
        String name = e.getLocalName();
        Configuration instance;

        try
        {
            if (depth > MAX_RECURSION_DEPTH)
            {
                throw new FileLoadException(String.format("Maximum recursion depth %s exceeded", MAX_RECURSION_DEPTH));
            }

            if (handleInternalElement(e, parent, parentType, scope, depth, predicate))
            {
                return;
            }

            Binder binder = getBinder(parentType, name);
            CompositeType type = binder.getType();
            instance = binder.getInstance(e, scope);
            if (binder.initInstance())
            {
                // initialise attributes
                mapAttributesToProperties(e, instance, type, predicate, scope);

                // interface based initialisation.
                if (Reference.class.isAssignableFrom(instance.getClass()))
                {
                    String referenceName = ((Reference) instance).getName();
                    if (referenceName != null && referenceName.length() > 0)
                    {
                        scope.addUnique((Reference) instance);
                    }
                }

                boolean loadType = predicate.loadType(instance, e);
                if (loadType)
                {
                    scope = scope.createChild();

                    // initialise sub-elements.
                    loadSubElements(e, instance, type, scope, depth, predicate);
                }

                binder.set(parent, instance);

                if (loadType)
                {
                    if (InitComponent.class.isAssignableFrom(instance.getClass()))
                    {
                        ((InitComponent) instance).initAfterChildren();
                    }
                }

                // Apply declarative validation
                if (predicate.validate(instance, e))
                {
                    validate(instance);
                }
            }
            else
            {
                binder.set(parent, instance);
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

    private Binder getBinder(CompositeType parentType, String elementName) throws FileLoadException
    {
        String propertyName = convertLocalNameToPropertyName(elementName);
        Binder binder = getAdderByName(parentType, propertyName);
        if (binder == null)
        {
            CompositeType type = typeDefinitions.get(elementName);
            if (type == null)
            {
                throw new FileLoadException("Unknown child element '" + elementName + "'");
            }

            binder = getAdderByType(parentType, type);
            if (binder == null)
            {
                binder = getSetter(parentType, type);

                if (binder == null)
                {
                    binder = new Loader(type);
                }
            }
        }

        return binder;
    }

    private Binder getAdderByName(CompositeType parentType, String propertyName) throws FileLoadException
    {
        for (TypeProperty property: parentType.getProperties(CollectionType.class))
        {
            Addable annotation = property.getAnnotation(Addable.class);
            if (annotation != null && propertyName.equals(annotation.value()))
            {
                Type propertyType = property.getType();
                Type targetType = propertyType.getTargetType();
                if (targetType instanceof CompositeType)
                {
                    return new NestedAdder(property, (CompositeType) targetType);
                }
                else if (targetType instanceof ReferenceType)
                {
                    return new ReferenceAdder(property, (ReferenceType) targetType, annotation.reference());
                }
                else
                {
                    throw new FileLoadException("@Addable property for unsupported type '" + targetType.toString() + "'");
                }
            }
        }

        return null;
    }

    private Binder getAdderByType(CompositeType parentType, CompositeType type)
    {
        for (TypeProperty property: parentType.getProperties(CollectionType.class))
        {
            Type targetType = property.getType().getTargetType();
            if (targetType instanceof CompositeType && targetType.getClazz().isAssignableFrom(type.getClazz()))
            {
                return new NestedAdder(property, type);
            }
        }

        return null;
    }

    private Binder getSetter(CompositeType parentType, CompositeType type)
    {
        for (TypeProperty property: parentType.getProperties(CompositeType.class))
        {
            if (property.getType().getClazz().isAssignableFrom(type.getClazz()))
            {
                return new Setter(property);
            }
        }

        return null;
    }

    private interface Binder
    {
        CompositeType getType();

        Configuration getInstance(Element element, Scope scope) throws Exception;

        boolean initInstance();

        void set(Configuration parent, Configuration instance) throws Exception;
    }

    private abstract class CreatingBinder implements Binder
    {
        public boolean initInstance()
        {
            return true;
        }

        public Configuration getInstance(Element element, Scope scope) throws Exception
        {
            return create(getType());
        }
    }

    private class Loader extends CreatingBinder
    {
        private CompositeType type;

        public Loader(CompositeType type)
        {
            this.type = type;
        }

        public CompositeType getType()
        {
            return type;
        }

        public void set(Configuration parent, Configuration instance) throws Exception
        {
            // Noop.
        }
    }

    private class Setter extends CreatingBinder
    {
        private TypeProperty property;

        public Setter(TypeProperty property)
        {
            this.property = property;
        }

        public CompositeType getType()
        {
            return (CompositeType) property.getType();
        }

        public void set(Configuration parent, Configuration instance) throws Exception
        {
            property.setValue(parent, instance);
        }
    }

    private abstract class Adder implements Binder
    {
        private TypeProperty property;

        protected Adder(TypeProperty property)
        {
            this.property = property;
        }

        public void set(Configuration parent, Configuration instance) throws Exception
        {
            try
            {
                CollectionType type = (CollectionType) property.getType();
                if (type instanceof ListType)
                {
                    @SuppressWarnings("unchecked")
                    List<? super Configuration> list = (List) property.getValue(parent);
                    list.add(instance);
                }
                else
                {
                    MapType mapType = (MapType) type;
                    CompositeType elementType = mapType.getTargetType();

                    @SuppressWarnings("unchecked")
                    Map<String, ? super Configuration> map = (Map) property.getValue(parent);
                    String key = (String) elementType.getProperty(mapType.getKeyProperty()).getValue(instance);
                    map.put(key, instance);
                }
            }
            catch (Exception e)
            {
                throw new FileLoadException("Unable to add value to property '" + property.getName() + "': " + e.getMessage(), e);
            }
        }
    }

    private class NestedAdder extends Adder
    {
        private CompositeType type;

        public CompositeType getType()
        {
            return type;
        }

        protected NestedAdder(TypeProperty property, CompositeType type)
        {
            super(property);
            this.type = type;
        }

        public Configuration getInstance(Element element, Scope scope) throws Exception
        {
            return create(type);
        }

        public boolean initInstance()
        {
            return true;
        }
    }

    private class ReferenceAdder extends Adder
    {
        private ReferenceType referenceType;
        private String attribute;

        private ReferenceAdder(TypeProperty property, ReferenceType referenceType, String attribute)
        {
            super(property);
            this.referenceType = referenceType;
            this.attribute = attribute;
        }

        public CompositeType getType()
        {
            return referenceType.getReferencedType();
        }

        public Configuration getInstance(Element element, Scope scope) throws Exception
        {
            String value = element.getAttributeValue(attribute);
            if (value == null)
            {
                throw new FileLoadException("Required attribute '" + attribute + "' not specified");
            }

            Object resolved = ReferenceResolver.resolveReference(value, scope);
            Class<? extends Configuration> clazz = referenceType.getReferencedType().getClazz();
            if (!clazz.isInstance(resolved))
            {
                throw new FileLoadException("Referenced value '" + value + "' has unexpected type (expected '" + clazz.getName() + "', got '" + resolved.getClass().getName() + "')");
            }

            return clazz.cast(resolved);
        }

        public boolean initInstance()
        {
            return false;
        }
    }

    private Configuration create(CompositeType type) throws FileLoadException
    {
        try
        {
            // FIXME loader should this always wire??
            return factory.buildBean(type.getClazz());
        }
        catch (Exception e)
        {
            throw new FileLoadException("Could not instantiate type '" + type.getClazz().getName() + "'. Reason: " + e.getMessage());
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

    private void loadSubElements(Element e, Configuration instance, CompositeType type, Scope scope, int depth, TypeLoadPredicate predicate)
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
                loadType(element, instance, type, scope, depth + 1, predicate);
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

        // FIXME loader better handling of text
        if (text != null && type.hasProperty("text"))
        {
            ReferenceResolver.ResolutionStrategy resolutionStrategy = getResolutionStrategy(predicate, type, e);

            text = ReferenceResolver.resolveReferences(text, scope, resolutionStrategy);
            type.getProperty("text").setValue(instance, text);
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

    private boolean handleInternalElement(Element element, Configuration instance, CompositeType type, Scope scope, int depth, TypeLoadPredicate predicate) throws Exception
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
                        loadSubElements(lae, instance, type, scope, depth, predicate);
                    }
                    catch (Exception e)
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

            if (!found)
            {
                throw new FileLoadException("Required attribute 'macro' not found");
            }

            return true;
        }
        else if (localName.equals("scope"))
        {
            // Just load children in new scope and redirect to parent
            loadSubElements(element, instance, type, scope.createChild(), depth, predicate);
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

        ParseException parseException = new ParseException(line, column, message.toString());
        parseException.initCause(t);
        return parseException;
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

    private void mapAttributesToProperties(Element source, Configuration target, CompositeType type, TypeLoadPredicate predicate, Scope scope) throws FileLoadException
    {
        for (int i = 0; i < source.getAttributeCount(); i++)
        {
            Attribute a = source.getAttribute(i);

            String propertyName = convertLocalNameToPropertyName(a.getLocalName());
            TypeProperty property = type.getProperty(propertyName);
            if (property == null)
            {
                throw new FileLoadException("Unrecognised attribute '" + propertyName + "'");
            }

            try
            {
                property.setValue(target, coerce(a.getValue(), property.getType(), getResolutionStrategy(predicate, target, source), scope));
            }
            catch (Exception e)
            {
                throw new FileLoadException(e.getMessage(), e);
            }
        }
    }

    private Object coerce(String value, Type type, ReferenceResolver.ResolutionStrategy resolutionStrategy, Scope scope) throws Exception
    {
        if (type instanceof SimpleType)
        {
            if (type instanceof ReferenceType)
            {
                return resolveReference(value, ((ReferenceType) type).getReferencedType().getClazz(), scope);
            }
            else
            {
                return Squeezers.findSqueezer(type.getClazz()).unsqueeze(ReferenceResolver.resolveReferences(value, scope, resolutionStrategy));
            }
        }
        else if (type instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) type;
            Class<? extends Configuration> clazz = compositeType.getClazz();

            if (Reference.class.isAssignableFrom(clazz))
            {
                return resolveReference(value, clazz, scope);
            }
            else
            {
                Constructor c = clazz.getConstructor(new Class[]{String.class});
                return c.newInstance(ReferenceResolver.resolveReferences(value, scope, resolutionStrategy));
            }
        }
        else if (type instanceof ListType)
        {
            ListType listType = (ListType) type;
            if (listType.getCollectionType() instanceof SimpleType)
            {
                TypeSqueezer squeezer = Squeezers.findSqueezer(listType.getCollectionType().getClazz());
                List<Object> values = new LinkedList<Object>();
                for (String v: ReferenceResolver.splitAndResolveReferences(value, scope, resolutionStrategy))
                {
                    values.add(squeezer.unsqueeze(v));
                }

                return values;
            }
        }

        throw new FileLoadException("Unable to convert value '" + value + "' to property type '" + type.toString() + "'");
    }

    private Object resolveReference(String rawReference, Class<? extends Configuration> expectedType, Scope scope) throws ResolutionException
    {
        Object obj = ReferenceResolver.resolveReference(rawReference, scope);
        if (!expectedType.isInstance(obj))
        {
            throw new ResolutionException("Referenced property '" + rawReference + "' has unexpected type.  Expected '" + expectedType.getName() + "', got '" + obj.getClass().getName() + "'");
        }

        return obj;
    }

    public void register(String name, CompositeType type)
    {
        typeDefinitions.put(name, type);
    }

    public boolean registered(String name)
    {
        return typeDefinitions.containsKey(name);
    }

    public void setObjectFactory(ObjectFactory factory)
    {
        this.factory = factory;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }
}