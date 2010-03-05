package com.zutubi.pulse.master.tove.classification;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.tove.classification.types.ClassifierType;
import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateNodeImpl;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.ListType;
import com.zutubi.util.bean.DefaultObjectFactory;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ClassificationManagerTest extends PulseTestCase
{
    public static final String CLASS_ANNOTATED = "csingle";
    public static final String CLASS_ANNOTATED_COLLECTION = "ccollection";
    public static final String CLASS_DYNAMIC = "it's classified";

    private static final String SINGLE_PATH = "single/path";
    private static final String COLLECTION_PATH = "collection/path";
    private static final String TEMPLATE_PATH = "template/path";
    private static final String CONCRETE_PATH = "concrete/path";
    private static final String ANNOTATED_PATH = "annotated/path";
    private static final String ANNOTATED_COLLECTION_PATH = "annotated/collection/path";
    private static final String CLASSIFIER_PATH = "classifier/path";

    private DefaultObjectFactory objectFactory = new DefaultObjectFactory();
    private ClassificationManager classificationManager;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        ConfigurationTemplateManager configurationTemplateManager = mock(ConfigurationTemplateManager.class);
        doReturn(null).when(configurationTemplateManager).getTemplateNode(SINGLE_PATH);
        doReturn(null).when(configurationTemplateManager).getTemplateNode(COLLECTION_PATH);
        doReturn(new TemplateNodeImpl(TEMPLATE_PATH, "id", false)).when(configurationTemplateManager).getTemplateNode(TEMPLATE_PATH);
        doReturn(new TemplateNodeImpl(CONCRETE_PATH, "id", true)).when(configurationTemplateManager).getTemplateNode(CONCRETE_PATH);

        CompositeType defaultsType = new CompositeType(DefaultsType.class, "configType");
        doReturn(defaultsType).when(configurationTemplateManager).getType(SINGLE_PATH);
        doReturn(new ListType(null, defaultsType, null)).when(configurationTemplateManager).getType(COLLECTION_PATH);

        CompositeType annotatedType = new CompositeType(AnnotatedType.class, "annotatedType");
        annotatedType.addAnnotation(AnnotatedType.class.getAnnotation(Classification.class));
        doReturn(annotatedType).when(configurationTemplateManager).getType(ANNOTATED_PATH);
        doReturn(new ListType(null, annotatedType, null)).when(configurationTemplateManager).getType(ANNOTATED_COLLECTION_PATH);

        CompositeType classifierType = new CompositeType(ClassifierType.class, "classifiedType");
        doReturn(classifierType).when(configurationTemplateManager).getType(CLASSIFIER_PATH);

        classificationManager = new ClassificationManager();
        classificationManager.setConfigurationTemplateManager(configurationTemplateManager);
        classificationManager.setObjectFactory(objectFactory);
    }

    public void testTemplate()
    {
        assertEquals(ClassificationManager.CLASS_TEMPLATE, classificationManager.classify(TEMPLATE_PATH));
    }

    public void testConcrete()
    {
        assertEquals(ClassificationManager.CLASS_CONCRETE, classificationManager.classify(CONCRETE_PATH));
    }

    public void testDefaultsComposite()
    {
        assertEquals(ClassificationManager.CLASS_SINGLE, classificationManager.classify(SINGLE_PATH));
    }

    public void testDefaultsCollection()
    {
        assertEquals(ClassificationManager.CLASS_COLLECTION, classificationManager.classify(COLLECTION_PATH));
    }

    public void testAnnotatedComposite()
    {
        assertEquals(CLASS_ANNOTATED, classificationManager.classify(ANNOTATED_PATH));
    }

    public void testAnnotatedCollection()
    {
        assertEquals(CLASS_ANNOTATED_COLLECTION, classificationManager.classify(ANNOTATED_COLLECTION_PATH));
    }

    public void testClassifier()
    {
        assertEquals(CLASS_DYNAMIC, classificationManager.classify(CLASSIFIER_PATH));
    }

    public static class DefaultsType extends AbstractConfiguration
    {
    }

    @Classification(single = CLASS_ANNOTATED, collection = CLASS_ANNOTATED_COLLECTION)
    public static class AnnotatedType extends AbstractConfiguration
    {
    }
}
