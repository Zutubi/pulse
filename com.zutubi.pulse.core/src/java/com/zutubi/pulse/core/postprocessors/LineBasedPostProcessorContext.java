package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.postprocessors.api.NameConflictResolution;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;

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

    public void addTestSuite(TestSuiteResult suite, NameConflictResolution conflictResolution)
    {
        delegate.addTestSuite(suite, conflictResolution);
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
