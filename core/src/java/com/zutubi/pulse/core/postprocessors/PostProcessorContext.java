package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.core.model.TestSuiteResult;

/**
 */
public interface PostProcessorContext
{
    TestSuiteResult getTestSuite();

    ResultState getResultState();

    void addFeature(Feature feature);

    void failCommand(String message);

    void errorCommand(String message);

    void addFeatureToCommand(Feature feature);
}
