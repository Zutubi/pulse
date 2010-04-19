package com.zutubi.pulse.core.marshal;

import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.Content;
import com.zutubi.pulse.core.engine.api.Referenceable;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.pulse.core.util.api.XMLUtils;
import com.zutubi.pulse.core.validation.PulseValidationContext;
import com.zutubi.pulse.core.validation.PulseValidationManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.squeezer.Squeezers;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.tove.type.*;
import com.zutubi.tove.variables.GenericVariable;
import com.zutubi.tove.variables.VariableResolver;
import com.zutubi.tove.variables.api.ResolutionException;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.io.IOUtils;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;
import com.zutubi.validation.i18n.MessagesTextProvider;
import nu.xom.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.zutubi.tove.variables.VariableResolver.ResolutionStrategy.RESOLVE_NON_STRICT;
import static com.zutubi.tove.variables.VariableResolver.ResolutionStrategy.RESOLVE_STRICT;

/**
 * Loads configuration objects from XML files, using the type properties and
 * annotations to determine how to bind attributes and child tags.
 *
 * @see ToveFileStorer
 */
public class ToveFileLoader
{
    private static final int MAX_RECURSION_DEPTH = 128;

    private TypeDefinitions typeDefinitions;
    private ObjectFactory factory;
    private TypeRegistry typeRegistry;
    private ValidationManager validationManager = new PulseValidationManager();

    public CompositeType lookupType(Class clazz) throws FileLoadException
    {
        CompositeType type = typeRegistry.getType(clazz);
        if (type == null)
        {
            throw new FileLoadException("Unable to find type for class '" + clazz.getName() + "'");
        }

        return type;
    }

    public void load(File file, Configuration root, FileResolver fileResolver) throws PulseException, FileNotFoundException
    {
        load(new FileInputStream(file), root, fileResolver);
    }

    public void load(InputStream input, Configuration root, FileResolver fileResolver) throws PulseException
    {
        load(input, root, null, fileResolver, null);
    }

    public void load(InputStream input, Configuration root, Scope globalScope, FileResolver fileResolver, ToveFileLoadInterceptor interceptor) throws PulseException
    {
        if (interceptor == null)
        {
            interceptor = new DefaultToveFileLoadInterceptor();
        }

        CompositeType type = lookupType(root.getClass());
        try
        {
            Builder builder = new Builder(new LocationAwareNodeFactory(null));

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

            mapAttributesToProperties(rootElement, root, type, interceptor, globalScope);

            for (int index = 0; index < rootElement.getChildCount(); index++)
            {
                Node node = rootElement.getChild(index);
                if (!(node instanceof Element))
                {
                    continue;
                }
                loadType((Element) node, root, type, globalScope, 1, new ToveFileResolver(fileResolver), interceptor);
            }
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    private void loadType(Element e, Configuration parent, CompositeType parentType, Scope scope, int depth, ToveFileResolver fileResolver, ToveFileLoadInterceptor interceptor) throws PulseException
    {
        String name = e.getLocalName();
        Object instance;

        try
        {
            if (depth > MAX_RECURSION_DEPTH)
            {
                throw new FileLoadException(String.format("Maximum recursion depth %s exceeded", MAX_RECURSION_DEPTH));
            }

            if (handleInternalElement(e, parent, parentType, scope, depth, fileResolver, interceptor))
            {
                return;
            }

            Binder binder = getBinder(parentType, name);
            VariableResolver.ResolutionStrategy resolutionStrategy = getResolutionStrategy(interceptor, parent, e);
            instance = binder.getInstance(e, scope, resolutionStrategy);
            if (binder.initInstance())
            {
                CompositeType type = binder.getType();
                Configuration configuration = (Configuration) instance;
                // initialise attributes
                mapAttributesToProperties(e, configuration, type, interceptor, scope);

                Referenceable referenceable = type.getAnnotation(Referenceable.class, true);
                if (referenceable != null)
                {
                    String referenceName = (String) type.getProperty(referenceable.nameProperty()).getValue(configuration);
                    if (StringUtils.stringSet(referenceName))
                    {
                        Object value;
                        if (referenceable.valueProperty().length() == 0)
                        {
                            value = configuration;
                        }
                        else
                        {
                            value = type.getProperty(referenceable.valueProperty()).getValue(configuration);
                        }
                        scope.addUnique(new GenericVariable<Object>(referenceName, value));
                    }
                }

                boolean loadType = interceptor.loadInstance(configuration, e, scope);
                if (loadType)
                {
                    scope = scope.createChild();

                    // initialise sub-elements.
                    loadSubElements(e, configuration, type, scope, depth, fileResolver, interceptor);
                }

                binder.set(parent, configuration);

                if (loadType)
                {
                    if (InitComponent.class.isAssignableFrom(configuration.getClass()))
                    {
                        ((InitComponent) configuration).initAfterChildren();
                    }
                }

                // Apply declarative validation
                if (interceptor.validate(configuration, e))
                {
                    validate(configuration, resolutionStrategy);
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
        Binder binder = getBinderByName(parentType, propertyName);
        if (binder == null)
        {
            CompositeType type = typeDefinitions.getType(elementName);
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

    private Binder getBinderByName(CompositeType parentType, String propertyName) throws FileLoadException
    {
        for (TypeProperty property: parentType.getProperties(CompositeType.class))
        {
            if (property.getName().equals(propertyName))
            {
                return new Setter(property, (CompositeType) property.getType());
            }
        }

        for (TypeProperty property: parentType.getProperties(CollectionType.class))
        {
            Addable annotation = property.getAnnotation(Addable.class);
            if (annotation != null && propertyName.equals(convertLocalNameToPropertyName(annotation.value())))
            {
                Type propertyType = property.getType();
                Type targetType = propertyType.getTargetType();
                if (targetType instanceof CompositeType)
                {
                    return new NestedAdder(property, (CompositeType) targetType);
                }
                else if (targetType instanceof ReferenceType)
                {
                    return new ReferenceAdder(property, (ReferenceType) targetType, annotation.attribute());
                }
                else if (targetType instanceof SimpleType)
                {
                    return new SimpleAdder(property, (SimpleType) targetType, annotation.attribute());
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
                return new Setter(property, type);
            }
        }

        return null;
    }

    /**
     * Interface for classes that know how to bind a child element to a
     * property in a composite config instance.
     */
    private interface Binder
    {
        /**
         * Indicates the type of the object corresponding to the child element.
         *
         * @return the type of the object identified or created for the child
         *         element
         */
        CompositeType getType();

        /**
         * Returns the instance specified by the child element, which may be
         * a new instance, or an existing one depending on the binding.
         *
         * @param element            the child element
         * @param scope              scope in which the element should be
         *                           loaded
         * @param resolutionStrategy specifies how to resolve references
         * @return the instance specified by the child element
         * @throws Exception on any error
         */
        Object getInstance(Element element, Scope scope, VariableResolver.ResolutionStrategy resolutionStrategy) throws Exception;

        /**
         * Indicates if the instance returned by {@link #getInstance(nu.xom.Element, com.zutubi.pulse.core.engine.api.Scope, com.zutubi.tove.variables.VariableResolver.ResolutionStrategy)}
         * should be initialised.
         *
         * @return true to indicate the instance requires initialisation, false
         *         otherwise
         */
        boolean initInstance();

        /**
         * Binds the actual instance to the appropriate property of the given
         * parent instance.
         *
         * @param parent   the parent instance to bind to
         * @param instance the child instance created by this binder
         * @throws Exception on any error
         */
        void set(Configuration parent, Object instance) throws Exception;
    }

    /**
     * Abstract base for binders which create a new object for the nested
     * element (as opposed to, for example, referencing an existing object).
     */
    private abstract class CreatingBinder implements Binder
    {
        public boolean initInstance()
        {
            return true;
        }

        public Object getInstance(Element element, Scope scope, VariableResolver.ResolutionStrategy resolutionStrategy) throws Exception
        {
            return create(getType());
        }
    }

    /**
     * A binder that just loads a child instance, but never sets it on the
     * parent.  Used for types registered at the top-level, which may nest
     * anywhere.
     */
    private class Loader extends CreatingBinder
    {
        private CompositeType type;

        /**
         * Creates a new loader.
         *
         * @param type the type of instance to create
         */
        public Loader(CompositeType type)
        {
            this.type = type;
        }

        public CompositeType getType()
        {
            return type;
        }

        public void set(Configuration parent, Object instance) throws Exception
        {
            // Noop.
        }
    }

    /**
     * A binder which sets the child instance to a singular composite property.
     */
    private class Setter extends CreatingBinder
    {
        private TypeProperty property;
        private CompositeType type;

        /**
         * Creates a setter.
         *
         * @param property the property of the parent instance to set
         * @param type     the type of the instance being set (may be a
         *                 subtype of the property type)
         */
        public Setter(TypeProperty property, CompositeType type)
        {
            this.property = property;
            this.type = type;
        }

        public CompositeType getType()
        {
            return type;
        }

        public void set(Configuration parent, Object instance) throws Exception
        {
            property.setValue(parent, instance);
        }
    }

    /**
     * Abstract base for binders which add the child instance to a collection
     * property.
     */
    private abstract class Adder implements Binder
    {
        private TypeProperty property;

        /**
         * Creates an adder.
         *
         * @param property the collection property of the parent instance to
         *                 which child instances are added
         */
        protected Adder(TypeProperty property)
        {
            this.property = property;
        }

        public void set(Configuration parent, Object instance) throws Exception
        {
            if (instance != null)
            {
                try
                {
                    CollectionType type = (CollectionType) property.getType();
                    if (type instanceof ListType)
                    {
                        @SuppressWarnings("unchecked")
                        List<Object> list = (List) property.getValue(parent);
                        list.add(instance);
                    }
                    else
                    {
                        MapType mapType = (MapType) type;
                        CompositeType elementType = mapType.getTargetType();
    
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map) property.getValue(parent);
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
    }

    /**
     * An adder which creates child instances from the child element before
     * adding them to the collection property.
     */
    private class NestedAdder extends Adder
    {
        private CompositeType type;

        /**
         * Creates a nested adder.
         *
         * @param property the collection property of the parent instance to
         *                 which child instances are added
         * @param type     the type of the child instance to create
         */
        protected NestedAdder(TypeProperty property, CompositeType type)
        {
            super(property);
            this.type = type;
        }

        public CompositeType getType()
        {
            return type;
        }

        public Object getInstance(Element element, Scope scope, VariableResolver.ResolutionStrategy resolutionStrategy) throws Exception
        {
            return create(type);
        }

        public boolean initInstance()
        {
            return true;
        }
    }

    /**
     * An adder which looks up child instances by reference before adding them
     * to the collection property.
     */
    private class ReferenceAdder extends Adder
    {
        private ReferenceType referenceType;
        private String attribute;

        /**
         * Creates a reference adder.
         *
         * @param property      the collection property of the parent instance
         *                      to which child instances are added
         * @param referenceType the reference type for items of the collection
         * @param attribute     the attribute of the child element that
         *                      contains the reference (may be empty to
         *                      indicate nested text content should be used)
         */
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

        public Object getInstance(Element element, Scope scope, VariableResolver.ResolutionStrategy resolutionStrategy) throws Exception
        {
            String value = getAddableValue(element, attribute);
            Object resolved = VariableResolver.resolveVariable(value, scope, resolutionStrategy);
            if (resolved == null)
            {
                return null;
            }
            else
            {
                Class<? extends Configuration> clazz = referenceType.getReferencedType().getClazz();
                if (!clazz.isInstance(resolved))
                {
                    throw new FileLoadException("Referenced value '" + value + "' has unexpected type (expected '" + clazz.getName() + "', got '" + resolved.getClass().getName() + "')");
                }

                return clazz.cast(resolved);
            }
        }

        public boolean initInstance()
        {
            return false;
        }
    }

    /**
     * An adder which creates simple child instances (i.e. primitives or enums)
     * before adding them to the collection property.
     */
    private class SimpleAdder extends Adder
    {
        private SimpleType type;
        private String attribute;

        /**
         * Creates a simple adder.
         *
         * @param property  the collection property of the parent instance to
         *                  which child instances are added
         * @param type      the simple type for items of the collection
         * @param attribute the attribute of the child element that contains
         *                  the simple value (may be empty to indicate nested
         *                  text content should be used)
         */
        protected SimpleAdder(TypeProperty property, SimpleType type, String attribute)
        {
            super(property);
            this.type = type;
            this.attribute = attribute;
        }

        public CompositeType getType()
        {
            return null;
        }

        public Object getInstance(Element element, Scope scope, VariableResolver.ResolutionStrategy resolutionStrategy) throws Exception
        {
            return coerce(getAddableValue(element, attribute), type, resolutionStrategy, scope);
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
            return factory.buildBean(type.getClazz());
        }
        catch (Exception e)
        {
            throw new FileLoadException("Could not instantiate type '" + type.getClazz().getName() + "'. Reason: " + e.getMessage());
        }
    }

    private String getAddableValue(Element element, String attribute) throws FileLoadException
    {
        String value;
        if (StringUtils.stringSet(attribute))
        {
            value = element.getAttributeValue(attribute);
            if (value == null)
            {
                throw new FileLoadException("Required attribute '" + attribute + "' not specified");
            }
        }
        else
        {
            value = XMLUtils.getText(element, "");
        }

        return value;
    }

    private void validate(Object obj, VariableResolver.ResolutionStrategy resolutionStrategy) throws FileLoadValidationException, ValidationException
    {
        ValidationContext validationContext = new PulseValidationContext(new MessagesTextProvider(obj));
        validationContext.setProperty(VariableResolver.ResolutionStrategy.class.getName(), resolutionStrategy.toString());
        validationManager.validate(obj, validationContext);
        if (validationContext.hasErrors())
        {
            throw new FileLoadValidationException(validationContext);
        }
    }

    private void loadSubElements(Element e, Configuration instance, CompositeType type, Scope scope, int depth, ToveFileResolver fileResolver, ToveFileLoadInterceptor interceptor)
            throws Exception
    {
        String text = null;

        for (int index = 0; index < e.getChildCount(); index++)
        {
            Node node = e.getChild(index);

            if (node instanceof Element)
            {
                Element element = (Element) node;
                loadType(element, instance, type, scope, depth + 1, fileResolver, interceptor);
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

        if (text != null)
        {
            TypeProperty contentProperty = findContentProperty(type);
            if (contentProperty != null)
            {
                text = VariableResolver.resolveVariables(text, scope, getResolutionStrategy(interceptor, instance, e));
                contentProperty.setValue(instance, text);
            }
        }
    }

    private TypeProperty findContentProperty(CompositeType type)
    {
        return CollectionUtils.find(type.getProperties(), new Predicate<TypeProperty>()
        {
            public boolean satisfied(TypeProperty property)
            {
                return property.getAnnotation(Content.class) != null;
            }
        });
    }

    private VariableResolver.ResolutionStrategy getResolutionStrategy(ToveFileLoadInterceptor interceptor, Configuration type, Element e)
    {
        return interceptor.allowUnresolved(type, e) ? RESOLVE_NON_STRICT : RESOLVE_STRICT;
    }

    private boolean handleInternalElement(Element element, Configuration instance, CompositeType type, Scope scope, int depth, ToveFileResolver fileResolver, ToveFileLoadInterceptor interceptor) throws Exception
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

                    Object o = VariableResolver.resolveVariable(macroName, scope, VariableResolver.ResolutionStrategy.RESOLVE_STRICT);
                    if (!LocationAwareElement.class.isAssignableFrom(o.getClass()))
                    {
                        throw new FileLoadException("Variable '" + macroName + "' does not resolve to a macro");
                    }

                    LocationAwareElement lae = (LocationAwareElement) o;

                    try
                    {
                        loadSubElements(lae, instance, type, scope, depth, fileResolver, interceptor);
                    }
                    catch (Exception e)
                    {
                        throw new FileLoadException("While expanding macro defined at " + lae.formatLocation() + ":\n" + indentMessage(e.getMessage()), e);
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
            loadSubElements(element, instance, type, scope.createChild(), depth, fileResolver, interceptor);
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

            if (!StringUtils.stringSet(path))
            {
                throw new FileLoadException("Required attribute 'path' not set");
            }


            InputStream input;
            if (optional)
            {
                try
                {
                    input = fileResolver.pushImport(path);
                }
                catch (Exception e)
                {
                    // Ignore for optional includes.
                    return true;
                }
            }
            else
            {
                input = fileResolver.pushImport(path);
            }

            try
            {
                Document doc = loadDocument(input, fileResolver.getCurrentPath());
                loadSubElements(doc.getRootElement(), instance, type, scope, depth, fileResolver, interceptor);
                return true;
            }
            catch (Exception e)
            {
                throw new FileLoadException("While importing file '" + path + "':\n" + indentMessage(e.getMessage()), e);
            }
            finally
            {
                fileResolver.popImport();
            }
        }

        return false;
    }

    private Document loadDocument(InputStream input, String file) throws ParsingException, IOException
    {
        try
        {
            Builder builder = new Builder(new LocationAwareNodeFactory(file));
            return builder.build(input);
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    private String indentMessage(String message)
    {
        return "  " + message.replaceAll("\\n", "\n  ");
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
            message.append("starting at ");
            message.append(location.formatLocation());
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

    private void mapAttributesToProperties(Element source, Configuration target, CompositeType type, ToveFileLoadInterceptor interceptor, Scope scope) throws FileLoadException
    {
        for (int i = 0; i < source.getAttributeCount(); i++)
        {
            Attribute a = source.getAttribute(i);

            String propertyName = convertLocalNameToPropertyName(a.getLocalName());
            TypeProperty property = type.getProperty(propertyName);
            if (property == null)
            {
                throw new FileLoadException("Unrecognised attribute '" + a.getLocalName() + "'");
            }

            try
            {
                Object value = coerce(a.getValue(), property.getType(), getResolutionStrategy(interceptor, target, source), scope);

                try
                {
                    property.setValue(target, value);
                }
                catch (Exception e)
                {
                    throw new FileLoadException(e.getMessage(), e);
                }
            }
            catch (Exception e)
            {
                throw new FileLoadException("Unable to convert value of attribute '" + a.getLocalName() + "' to expected type '" + property.getType().getClazz().getName() + "': " + e.getMessage(), e);
            }
        }
    }

    private Object coerce(String value, Type type, VariableResolver.ResolutionStrategy resolutionStrategy, Scope scope) throws Exception
    {
        if (type instanceof SimpleType)
        {
            if (type instanceof ReferenceType)
            {
                return resolveReference(value, ((ReferenceType) type).getReferencedType().getClazz(), resolutionStrategy, scope);
            }
            else
            {
                return Squeezers.findSqueezer(type.getClazz()).unsqueeze(VariableResolver.resolveVariables(value, scope, resolutionStrategy));
            }
        }
        else if (type instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) type;
            Class<? extends Configuration> clazz = compositeType.getClazz();

            if (compositeType.hasAnnotation(Referenceable.class, true))
            {
                return resolveReference(value, clazz, resolutionStrategy, scope);
            }
            else
            {
                try
                {
                    Constructor c = clazz.getConstructor(new Class[]{String.class});
                    return c.newInstance(VariableResolver.resolveVariables(value, scope, resolutionStrategy));
                }
                catch (Exception e)
                {
                    // Expected if there is no such constructor.  Fall through.
                }
            }
        }
        else if (type instanceof ListType)
        {
            ListType listType = (ListType) type;
            if (listType.getCollectionType() instanceof SimpleType)
            {
                TypeSqueezer squeezer = Squeezers.findSqueezer(listType.getCollectionType().getClazz());
                if (squeezer != null)
                {
                    List<Object> values = new LinkedList<Object>();
                    for (String v: VariableResolver.splitAndResolveVariable(value, scope, resolutionStrategy))
                    {
                        values.add(squeezer.unsqueeze(v));
                    }

                    return values;
                }
            }
        }

        throw new FileLoadException("No conversion available to property type '" + type.toString() + "'");
    }

    private Object resolveReference(String rawReference, Class<? extends Configuration> expectedType, VariableResolver.ResolutionStrategy resolutionStrategy, Scope scope) throws ResolutionException
    {
        if (!StringUtils.stringSet(rawReference))
        {
            return null;
        }
        
        Object obj = VariableResolver.resolveVariable(rawReference, scope, resolutionStrategy);
        if (obj != null && !expectedType.isInstance(obj))
        {
            throw new ResolutionException("Referenced property '" + rawReference + "' has unexpected type.  Expected '" + expectedType.getName() + "', got '" + obj.getClass().getName() + "'");
        }

        return obj;
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

    public void setTypeDefinitions(TypeDefinitions typeDefinitions)
    {
        this.typeDefinitions = typeDefinitions;
    }
}