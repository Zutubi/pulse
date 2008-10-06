package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.PathConsumingActionResolver;

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
