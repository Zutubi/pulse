package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.PathConsumingActionResolver;

/**
 */
public class StageTestsActionResolver extends PathConsumingActionResolver
{
    public StageTestsActionResolver(String stage)
    {
        super("viewTestSuite", "path");
        addParameter("stageName", stage);
    }
}
