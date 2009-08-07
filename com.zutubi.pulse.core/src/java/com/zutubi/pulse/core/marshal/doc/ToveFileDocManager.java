package com.zutubi.pulse.core.marshal.doc;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.Content;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.marshal.ToveFileStorer;
import com.zutubi.pulse.core.marshal.ToveFileUtils;
import static com.zutubi.pulse.core.marshal.ToveFileUtils.convertPropertyNameToLocalName;
import com.zutubi.pulse.core.marshal.TypeDefinitions;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.config.api.ConfigurationExample;
import com.zutubi.tove.config.docs.ConfigurationDocsManager;
import com.zutubi.tove.config.docs.PropertyDocs;
import com.zutubi.tove.config.docs.TypeDocs;
import com.zutubi.tove.type.*;
import com.zutubi.util.CollectionUtils;
import static com.zutubi.util.CollectionUtils.map;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import static com.zutubi.util.reflection.MethodPredicates.*;
import com.zutubi.validation.annotations.Required;
import nu.xom.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import static java.util.Arrays.asList;
import java.util.*;

/**
 * Analyses types and creates data structures representing tove file
 * documentation for those types.  This documentation is XML-centric,
 * identifying elements, attributes, text content and associated
 * descriptions.
 */
public class ToveFileDocManager
{
    private static final Logger LOG = Logger.getLogger(ToveFileDocManager.class);
    private static final Messages I18N = Messages.getInstance(ToveFileDocManager.class);

    private static final String KEY_SUFFIX_ADDABLE_ATTRIBUTE = "addable.attribute";
    private static final String KEY_SUFFIX_ADDABLE_BRIEF     = "addable.brief";
    private static final String KEY_SUFFIX_ADDABLE_CONTENT   = "addable.content";
    private static final String KEY_SUFFIX_ADDABLE_VERBOSE   = "addable.verbose";
    private static final String KEY_SUFFIX_BUILTIN           = "builtin";
    private static final String KEY_SUFFIX_CONTENT           = "content";
    private static final String KEY_PREFIX_EXAMPLE           = "example";
    private static final String KEY_SUFFIX_EXAMPLE_BLURB     = "blurb";

    private static final String EXAMPLE_METHOD_PREFIX = "get";

    private static final List<ChildNodeDocs> BUILTINS = map(asList("import", "macro", "macro-ref", "scope"), new Mapping<String, ChildNodeDocs>()
    {
        public ChildNodeDocs map(String name)
        {
            String doc = I18N.format(name + "." + KEY_SUFFIX_BUILTIN);
            return new ChildNodeDocs(name, new BuiltinElementDocs(doc, doc), Arity.ZERO_OR_MORE);
        }
    });

    private Map<String, ElementDocs> rootElements = new HashMap<String, ElementDocs>();
    private Map<CompositeType, ElementDocs> concreteCache = new HashMap<CompositeType, ElementDocs>();
    private Map<CompositeType, ExtensibleDocs> extensibleCache = new HashMap<CompositeType, ExtensibleDocs>();
    private ConfigurationDocsManager configurationDocsManager;
    private ObjectFactory objectFactory;
    private PulseFileLoaderFactory fileLoaderFactory;

    /**
     * Registers a root element for a specific type of file.  The root will be
     * loaded into an instance of the given type.  The generated documentation
     * is returned, and may also be accessed later using {@link #lookupRoot(String)}.
     *
     * @param rootName        name of the root element, which is also used to
     *                        store the documentation for later reference and
     *                        thus should uniquely identify the file type
     * @param rootType        type of the root element (i.e. the type of
     *                        instance that is stored in this type of file)
     * @param typeDefinitions top-level type definitions for this kind of file
     * @return the generated documentation for the root (and, recursively,
     *         everything it can contain)
     */
    public synchronized ElementDocs registerRoot(String rootName, CompositeType rootType, TypeDefinitions typeDefinitions)
    {
        ElementDocs docs = getDocs(rootType, typeDefinitions);
        for (ChildNodeDocs builtin: BUILTINS)
        {
            docs.addChild(builtin);
        }
        rootElements.put(rootName, docs);
        return docs;
    }

    /**
     * Returns the set of all registered roots.  The set may not be modified.
     *
     * @return the names of all roots registered with this manager
     *
     * @see #registerRoot(String, com.zutubi.tove.type.CompositeType, com.zutubi.pulse.core.marshal.TypeDefinitions)
     */
    public synchronized Set<String> getRoots()
    {
        return Collections.unmodifiableSet(rootElements.keySet());
    }

    /**
     * Retrieves the documentation for the file type identified by the given
     * root name.  This name should have been passed to {@link #registerRoot(String, com.zutubi.tove.type.CompositeType, com.zutubi.pulse.core.marshal.TypeDefinitions)}
     * previously.
     *
     * @param rootName name of the file type to look up the documentation for
     * @return the documentation for the file type, or null if no file has been
     *         registered under this name
     */
    public synchronized ElementDocs lookupRoot(String rootName)
    {
        return rootElements.get(rootName);
    }

    /**
     * Dynamically registers a given type by hooking it in as an extension to
     * any already-registered super types.  Useful for pluggable extensions.
     *
     * @param name            the name under which the type is registered with
     *                        the file loader/storer
     * @param type            the type to register
     * @param typeDefinitions all top-level type definitions for the type of
     *                        file the type appears within
     */
    public synchronized void registerType(String name, CompositeType type, TypeDefinitions typeDefinitions)
    {
        ElementDocs elementDocs = getDocs(type, typeDefinitions);
        for (ExtensibleDocs extensibleDocs: getExtensibleParents(type))
        {
            extensibleDocs.addExtension(name, elementDocs);
        }
    }

    /**
     * Dynamically unregisters a type by unhooking it as an extension from any
     * super types.  Useful for removal of plugabble extensions.
     *
     * @param name name under which the type was registered
     * @param type the type to unregister
     */
    public synchronized void unregisterType(String name, CompositeType type)
    {
        for (ExtensibleDocs extensibleDocs: getExtensibleParents(type))
        {
            extensibleDocs.removeExtension(name);
        }
    }

    private List<ExtensibleDocs> getExtensibleParents(CompositeType type)
    {
        List<ExtensibleDocs> result = new LinkedList<ExtensibleDocs>();

        Set<CompositeType> superTypeSet = new HashSet<CompositeType>();
        collectSuperTypes(type, superTypeSet);
        for (CompositeType superType: superTypeSet)
        {
            ExtensibleDocs extensibleDocs = extensibleCache.get(superType);
            if (extensibleDocs != null)
            {
                result.add(extensibleDocs);
            }
        }

        return result;
    }

    private void collectSuperTypes(CompositeType type, Set<CompositeType> superTypeSet)
    {
        for (CompositeType superType: type.getSuperTypes())
        {
            if (superTypeSet.add(superType))
            {
                collectSuperTypes(superType, superTypeSet);
            }
        }
    }

    private ElementDocs getDocs(CompositeType type, TypeDefinitions typeDefinitions)
    {
        ElementDocs docs = concreteCache.get(type);
        if (docs == null)
        {
            TypeDocs typeDocs = configurationDocsManager.getDocs(type);
            if (!StringUtils.stringSet(typeDocs.getBrief()))
            {
                LOG.warning("Documentation for type '" + type.getClazz().getName() + "' is missing 'introduction'");
            }

            if (!StringUtils.stringSet(typeDocs.getVerbose()))
            {
                LOG.warning("Documentation for type '" + type.getClazz().getName() + "' is missing 'verbose'");
            }

            Messages messages = Messages.getInstance(type.getClazz());
            docs = new ElementDocs(typeDocs.getBrief(), typeDocs.getVerbose());
            concreteCache.put(type, docs);

            for (TypeProperty property: type.getProperties())
            {
                if (ToveFileUtils.convertsToAttribute(property))
                {
                    addAttribute(docs, type, property, typeDocs);
                }
                else if (property.getAnnotation(Content.class) != null)
                {
                    docs.setContentDocs(new ContentDocs(formatProperty(messages, type, property, KEY_SUFFIX_CONTENT)));
                }
                else
                {
                    addChildElements(docs, type, property, typeDefinitions, messages);
                }
            }

            getExamples(docs, type, messages);
        }

        return docs;
    }

    private void getExamples(ElementDocs docs, CompositeType type, Messages messages)
    {
        Class<?> examplesClass = ConventionSupport.getExamples(type.getClazz());
        if (examplesClass != null)
        {
            List<Method> exampleMethods = CollectionUtils.filter(examplesClass.getMethods(),
                    and(hasPrefix(EXAMPLE_METHOD_PREFIX, false), acceptsParameters(), returnsType(ConfigurationExample.class)));

            try
            {
                Object examplesInstance = objectFactory.buildBean(examplesClass);
                for (Method exampleMethod: exampleMethods)
                {
                    ConfigurationExample example = (ConfigurationExample) exampleMethod.invoke(examplesInstance);
                    if (example != null)
                    {
                        addExample(docs, example, getExampleName(exampleMethod), messages);
                    }
                }
            }
            catch (Exception e)
            {
                LOG.warning("Cannot get examples for type '" + type.getClazz().getName() + "': " + e.getMessage(), e);
            }
        }
    }

    private String getExampleName(Method exampleMethod)
    {
        String namePart = exampleMethod.getName().substring(EXAMPLE_METHOD_PREFIX.length());
        return Character.toLowerCase(namePart.charAt(0)) + namePart.substring(1);
    }

    private void addExample(ElementDocs docs, ConfigurationExample example, String name, Messages messages) throws IOException
    {
        ToveFileStorer toveFileStorer = fileLoaderFactory.createStorer();
        ByteArrayOutputStream baos = null;
        try
        {
            baos = new ByteArrayOutputStream();
            Element root = new Element(example.getElement());
            toveFileStorer.store(baos, example.getConfiguration(), root);

            String exampleString = new String(baos.toByteArray());
            // Strip the <?xml ... ?> header line
            String[] split = StringUtils.getNextToken(exampleString, '\n', true);
            if (split != null)
            {
                exampleString = split[1].trim();
                String blurb = messages.format(StringUtils.join(".", KEY_PREFIX_EXAMPLE, name, KEY_SUFFIX_EXAMPLE_BLURB));
                docs.addExample(new ExampleDocs(name, blurb, exampleString));
            }
        }
        finally
        {
            IOUtils.close(baos);
        }
    }

    private void addAttribute(ElementDocs element, CompositeType type, TypeProperty property, TypeDocs typeDocs)
    {
        PropertyDocs propertyDocs = typeDocs.getPropertyDocs(property.getName());
        try
        {
            element.addAttribute(new AttributeDocs(convertPropertyNameToLocalName(property.getName()), propertyDocs.getVerbose(), isRequired(property), getDefaultValue(type, property)));
        }
        catch (Exception e)
        {
            LOG.warning(e);
        }
    }

    private boolean isRequired(TypeProperty property)
    {
        return property.getAnnotation(Required.class) != null;
    }

    private String getDefaultValue(CompositeType type, TypeProperty property) throws Exception
    {
        String value = ToveFileUtils.convertAttribute(type.getDefaultInstance(), type, property);
        if (value == null)
        {
            value = "";
        }

        return value;
    }

    private void addChildElements(ElementDocs element, CompositeType parentType, TypeProperty property, TypeDefinitions typeDefinitions, Messages messages)
    {
        Type type = property.getType();
        if (type instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) type;
            if (compositeType.isExtendable())
            {
                addExtensions(element, property.getName(), compositeType, typeDefinitions, Arity.ZERO_OR_ONE);
            }
            else
            {
                element.addChild(new ChildNodeDocs(getChildName(property, typeDefinitions), getDocs((CompositeType) type, typeDefinitions), isRequired(property) ? Arity.EXACTLY_ONE : Arity.ZERO_OR_ONE));
            }
        }
        else if (type instanceof CollectionType)
        {
            Type itemType = type.getTargetType();
            if (itemType instanceof SimpleType)
            {
                Addable addable = property.getAnnotation(Addable.class);
                if (addable != null)
                {
                    element.addChild(new ChildNodeDocs(addable.value(), getAddableDocs(parentType, property, addable, messages), Arity.ZERO_OR_MORE));
                }
            }
            else if (itemType instanceof CompositeType)
            {
                CompositeType compositeType = (CompositeType) itemType;
                if (compositeType.isExtendable())
                {
                    addExtensions(element, property.getName(), compositeType, typeDefinitions,  Arity.ZERO_OR_MORE);
                }
                else
                {
                    String name;
                    Addable addable = property.getAnnotation(Addable.class);
                    if (addable == null)
                    {
                        name = convertPropertyNameToLocalName(typeDefinitions.getName(compositeType));
                    }
                    else
                    {
                        name = addable.value();
                    }

                    if (name != null)
                    {
                        element.addChild(new ChildNodeDocs(name, getDocs(compositeType, typeDefinitions), Arity.ZERO_OR_MORE));
                    }
                }
            }
        }
    }

    private void addExtensions(ElementDocs element, String name, CompositeType compositeType, TypeDefinitions typeDefinitions, Arity arity)
    {
        element.addChild(new ChildNodeDocs(convertPropertyNameToLocalName(name), getExtensibleDocs(compositeType, typeDefinitions), arity));
    }

    private ExtensibleDocs getExtensibleDocs(CompositeType compositeType, TypeDefinitions typeDefinitions)
    {
        ExtensibleDocs extensibleDocs = extensibleCache.get(compositeType);
        if (extensibleDocs == null)
        {
            TypeDocs typeDocs = configurationDocsManager.getDocs(compositeType);
            extensibleDocs = new ExtensibleDocs(typeDocs.getBrief(), typeDocs.getVerbose());
            extensibleCache.put(compositeType, extensibleDocs);

            for (CompositeType childType: compositeType.getExtensions())
            {
                String extensionName = typeDefinitions.getName(childType);
                if (extensionName != null)
                {
                    extensibleDocs.addExtension(extensionName, getDocs(childType, typeDefinitions));
                }
            }

        }

        return extensibleDocs;
    }

    private ElementDocs getAddableDocs(CompositeType type, TypeProperty property, Addable addable, Messages messages)
    {
        ElementDocs elementDocs = new ElementDocs(formatProperty(messages, type, property, KEY_SUFFIX_ADDABLE_BRIEF), formatProperty(messages, type, property, KEY_SUFFIX_ADDABLE_VERBOSE));

        String attribute = addable.attribute();
        if (StringUtils.stringSet(attribute))
        {
            elementDocs.addAttribute(new AttributeDocs(convertPropertyNameToLocalName(attribute), formatProperty(messages, type, property, KEY_SUFFIX_ADDABLE_ATTRIBUTE), true, ""));
        }
        else
        {
            elementDocs.setContentDocs(new ContentDocs(formatProperty(messages, type, property, KEY_SUFFIX_ADDABLE_CONTENT)));
        }

        return elementDocs;
    }

    private String getChildName(TypeProperty property, TypeDefinitions typeDefinitions)
    {
        String name = typeDefinitions.getName((CompositeType) property.getType());
        if (name == null)
        {
            name = property.getName();
        }

        return convertPropertyNameToLocalName(name);
    }

    private String formatProperty(Messages messages, CompositeType type, TypeProperty property, String suffix)
    {
        String key = property.getName() + "." + suffix;
        if (messages.isKeyDefined(key))
        {
            return messages.format(key);
        }
        else
        {
            LOG.warning("Expected i18n key '" + key + "' not defined to document property '" + property.getName() + "' of type '" + type.getClazz().getName() + "'");
            return "No details";
        }
    }

    public void setConfigurationDocsManager(ConfigurationDocsManager configurationDocsManager)
    {
        this.configurationDocsManager = configurationDocsManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }
}
