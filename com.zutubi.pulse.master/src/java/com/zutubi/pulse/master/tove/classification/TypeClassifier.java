package com.zutubi.pulse.master.tove.classification;

import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Classifier;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;


/**
 * Handles classification for a single composite type.
 *
 * @see com.zutubi.pulse.master.tove.classification.ClassificationManager
 */
public class TypeClassifier
{
    private static final Logger LOG = Logger.getLogger(TypeClassifier.class);

    private Classifier<Configuration> classifier;
    private String collectionClass = ClassificationManager.CLASS_COLLECTION;
    private String singleClass = ClassificationManager.CLASS_SINGLE;
    private ConfigurationTemplateManager configurationTemplateManager;

    public TypeClassifier(CompositeType type, ObjectFactory objectFactory, ConfigurationTemplateManager configurationTemplateManager)
    {
        initClassifier(type, objectFactory);

        Classification classification = type.getAnnotation(Classification.class, true);
        if (classification != null)
        {
            if(StringUtils.stringSet(classification.single()))
            {
                singleClass = classification.single();
            }

            if (StringUtils.stringSet(classification.collection()))
            {
                collectionClass = classification.collection();
            }
        }

        this.configurationTemplateManager = configurationTemplateManager;
    }

    @SuppressWarnings({"unchecked"})
    private void initClassifier(CompositeType type, ObjectFactory objectFactory)
    {
        Class classifierClass = ConventionSupport.getClassifier(type.getClazz());
        if (classifierClass != null)
        {
            try
            {
                if (Classifier.class.isAssignableFrom(classifierClass))
                {
                    classifier = (Classifier<Configuration>) objectFactory.buildBean(classifierClass);
                }
                else
                {
                    LOG.warning("Ignoring classifier with class '" + classifierClass.getName() + "' which does not implement '" + Classifier.class.getName() + "'");
                }
            }
            catch (Exception e)
            {
                LOG.severe("Unable to build classifier of type '" + classifierClass.getName() + "': " + e.getMessage(), e);
            }
        }
    }

    public String classify(boolean collection, String path)
    {
        if (collection)
        {
            return collectionClass;
        }
        else if (classifier != null)
        {
            Configuration instance = configurationTemplateManager.getInstance(path, Configuration.class);
            return classifier.classify(instance);
        }
        else
        {
            return singleClass;
        }
    }
}
