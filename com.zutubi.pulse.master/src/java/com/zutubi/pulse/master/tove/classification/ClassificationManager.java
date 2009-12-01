package com.zutubi.pulse.master.tove.classification;

import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;
import com.zutubi.util.bean.ObjectFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the ability to classify types/instances for display in the UI.  This
 * allows them, for example, to be presented with appropriate icons.
 */
public class ClassificationManager
{
    public static final String CLASS_SINGLE = "composite";
    public static final String CLASS_COLLECTION = "collection";
    public static final String CLASS_CONCRETE = "concrete";
    public static final String CLASS_TEMPLATE = "template";

    private Map<CompositeType, TypeClassifier> classifiersByType = new HashMap<CompositeType, TypeClassifier>();

    private ConfigurationTemplateManager configurationTemplateManager;
    private ObjectFactory objectFactory;

    /**
     * Returns the classification for a given path.  The path must be valid
     * enough to determine its type.
     *
     * @param path the path to classify
     * @return the class of the path
     */
    public String classify(String path)
    {
        TemplateNode templateNode = configurationTemplateManager.getTemplateNode(path);
        if (templateNode == null)
        {
            ComplexType type = configurationTemplateManager.getType(path);
            boolean isCollection = type instanceof CollectionType;
            Type targetType = type.getTargetType();
            if (targetType instanceof CompositeType)
            {
                TypeClassifier typeClassifier = getClassifier((CompositeType) targetType);
                return typeClassifier.classify(isCollection, path);
            }

            return isCollection ? CLASS_COLLECTION : CLASS_SINGLE;
        }
        else
        {
            return templateNode.isConcrete() ? CLASS_CONCRETE : CLASS_TEMPLATE;
        }
    }

    private TypeClassifier getClassifier(CompositeType type)
    {
        TypeClassifier typeClassifier = classifiersByType.get(type);
        if (typeClassifier == null)
        {
            typeClassifier = new TypeClassifier(type, objectFactory, configurationTemplateManager);
            classifiersByType.put(type, typeClassifier);
        }

        return typeClassifier;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
