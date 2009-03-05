package com.zutubi.pulse.core.marshal.doc;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.Content;
import com.zutubi.pulse.core.marshal.ToveFileUtils;
import com.zutubi.pulse.core.marshal.TypeDefinitions;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.docs.ConfigurationDocsManager;
import com.zutubi.tove.config.docs.PropertyDocs;
import com.zutubi.tove.config.docs.TypeDocs;
import com.zutubi.tove.type.*;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.annotations.Required;

import java.util.HashMap;
import java.util.Map;

/**
 * Analyses types and creates data structures representing tove file
 * documentation for those types.  This documentation is XML-centric,
 * identifying elements, attributes, text content and associated
 * descriptions.
 */
public class ToveFileDocManager
{
    private static final Logger LOG = Logger.getLogger(ToveFileDocManager.class);

    private static final String KEY_SUFFIX_ADDABLE_ATTRIBUTE = "addable.attribute";
    private static final String KEY_SUFFIX_ADDABLE_BRIEF     = "addable.brief";
    private static final String KEY_SUFFIX_ADDABLE_CONTENT   = "addable.content";
    private static final String KEY_SUFFIX_ADDABLE_VERBOSE   = "addable.verbose";
    private static final String KEY_SUFFIX_CONTENT           = "content";

    private Map<String, ElementDocs> rootElements = new HashMap<String, ElementDocs>();
    private Map<CompositeType, ElementDocs> cache = new HashMap<CompositeType, ElementDocs>();
    private ConfigurationDocsManager configurationDocsManager;

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
        rootElements.put(rootName, docs);
        return docs;
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
    
    private ElementDocs getDocs(CompositeType type, TypeDefinitions typeDefinitions)
    {
        ElementDocs docs = cache.get(type);
        if (docs == null)
        {
            TypeDocs typeDocs = configurationDocsManager.getDocs(type);
            Messages messages = Messages.getInstance(type.getClazz());
            docs = new ElementDocs(typeDocs.getBrief(), typeDocs.getVerbose());

            try
            {
                Configuration defaultInstance = type.getClazz().newInstance();
                for (TypeProperty property: type.getProperties())
                {
                    if (ToveFileUtils.convertsToAttribute(property))
                    {
                        addAttribute(docs, type, property, defaultInstance, typeDocs);
                    }
                    else if (property.getAnnotation(Content.class) != null)
                    {
                        docs.setContent(new ContentDocs(formatProperty(messages, property, KEY_SUFFIX_CONTENT)));
                    }
                    else
                    {
                        addChildElements(docs, property, typeDefinitions, messages);
                    }
                }
            }
            catch (InstantiationException e)
            {
                LOG.warning(e);
            }
            catch (IllegalAccessException e)
            {
                LOG.warning(e);
            }

            cache.put(type, docs);
        }

        return docs;
    }

    private void addAttribute(ElementDocs element, CompositeType type, TypeProperty property, Configuration defaultInstance, TypeDocs typeDocs)
    {
        PropertyDocs propertyDocs = typeDocs.getPropertyDocs(property.getName());
        try
        {
            element.addAttribute(new AttributeDocs(property.getName(), propertyDocs.getVerbose(), isRequired(property), getDefaultValue(type, property, defaultInstance)));
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

    private String getDefaultValue(CompositeType type, TypeProperty property, Configuration defaultInstance) throws Exception
    {
        String value = ToveFileUtils.convertAttribute(defaultInstance, type, property);
        if (value == null)
        {
            value = "";
        }

        return value;
    }

    private void addChildElements(ElementDocs element, TypeProperty property, TypeDefinitions typeDefinitions, Messages messages)
    {
        Type type = property.getType();
        if (type instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) type;
            if (compositeType.isExtendable())
            {
                addExtensions(element, compositeType, typeDefinitions, Arity.ZERO_OR_ONE);
            }
            else
            {
                element.addChild(new ChildElementDocs(getChildName(property, typeDefinitions), getDocs((CompositeType) type, typeDefinitions), isRequired(property) ? Arity.EXACTLY_ONE : Arity.ZERO_OR_ONE));
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
                    element.addChild(new ChildElementDocs(addable.value(), getAddableDocs(property, addable, messages), Arity.ZERO_OR_MORE));
                }
            }
            else if (itemType instanceof CompositeType)
            {
                CompositeType compositeType = (CompositeType) itemType;
                if (compositeType.isExtendable())
                {
                    addExtensions(element, compositeType, typeDefinitions, Arity.ZERO_OR_MORE);
                }
                else
                {
                    Addable addable = property.getAnnotation(Addable.class);
                    if (addable != null)
                    {
                        element.addChild(new ChildElementDocs(addable.value(), getDocs(compositeType, typeDefinitions), Arity.ZERO_OR_MORE));
                    }
                }
            }
        }
    }

    private void addExtensions(ElementDocs element, CompositeType compositeType, TypeDefinitions typeDefinitions, Arity arity)
    {
        for (CompositeType childType: compositeType.getExtensions())
        {
            String name = typeDefinitions.getName(childType);
            if (name != null)
            {
                element.addChild(new ChildElementDocs(name, getDocs(childType, typeDefinitions), arity));
            }
        }
    }

    private ElementDocs getAddableDocs(TypeProperty property, Addable addable, Messages messages)
    {
        ElementDocs elementDocs = new ElementDocs(formatProperty(messages, property, KEY_SUFFIX_ADDABLE_BRIEF), formatProperty(messages, property, KEY_SUFFIX_ADDABLE_VERBOSE));

        String attribute = addable.attribute();
        if (TextUtils.stringSet(attribute))
        {
            elementDocs.addAttribute(new AttributeDocs(attribute, formatProperty(messages, property, KEY_SUFFIX_ADDABLE_ATTRIBUTE), true, ""));
        }
        else
        {
            elementDocs.setContent(new ContentDocs(formatProperty(messages, property, KEY_SUFFIX_ADDABLE_CONTENT)));
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
        
        return name;
    }

    private String formatProperty(Messages messages, TypeProperty property, String suffix)
    {
        return messages.format(property.getName() + "." + suffix);
    }

    public void setConfigurationDocsManager(ConfigurationDocsManager configurationDocsManager)
    {
        this.configurationDocsManager = configurationDocsManager;
    }
}
