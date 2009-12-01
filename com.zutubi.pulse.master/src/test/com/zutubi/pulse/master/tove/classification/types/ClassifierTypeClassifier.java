package com.zutubi.pulse.master.tove.classification.types;

import com.zutubi.pulse.master.tove.classification.ClassificationManagerTest;
import com.zutubi.tove.config.api.Classifier;

/**
 * Testing classifier for {@link ClassifierType}.
 */
public class ClassifierTypeClassifier implements Classifier<ClassifierType>
{
    public String classify(ClassifierType instance)
    {
        return ClassificationManagerTest.CLASS_DYNAMIC;
    }
}
