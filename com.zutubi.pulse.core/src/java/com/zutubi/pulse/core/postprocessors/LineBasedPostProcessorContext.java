package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.core.model.TestSuiteResult;

/**
 */
public class LineBasedPostProcessorContext implements PostProcessorContext
{
    private PostProcessorContext delegate;
    private Feature previousFeature = null;

    public LineBasedPostProcessorContext(PostProcessorContext delegate)
    {
        this.delegate = delegate;
    }

    public PostProcessorContext getDelegate()
    {
        return delegate;
    }

    public TestSuiteResult getTestSuite()
    {
        return delegate.getTestSuite();
    }

    public ResultState getResultState()
    {
        return delegate.getResultState();
    }

    public void addFeature(Feature feature)
    {
        previousFeature = feature;
        delegate.addFeature(feature);
    }

    public void failCommand(String message)
    {
        delegate.failCommand(message);
    }

    public void errorCommand(String message)
    {
        delegate.errorCommand(message);
    }

    public void addFeatureToCommand(Feature feature)
    {
        delegate.addFeatureToCommand(feature);
    }

    public Feature getPreviousFeature()
    {
        return previousFeature;
    }
}
