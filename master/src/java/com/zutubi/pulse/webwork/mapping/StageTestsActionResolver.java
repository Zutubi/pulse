package com.zutubi.pulse.webwork.mapping;

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
