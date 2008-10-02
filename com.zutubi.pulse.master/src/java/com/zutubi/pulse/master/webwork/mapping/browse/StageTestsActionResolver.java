package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.PathConsumingActionResolver;

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
