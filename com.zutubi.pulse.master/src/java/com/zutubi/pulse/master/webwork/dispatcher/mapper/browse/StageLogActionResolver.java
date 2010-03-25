package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves to the log page for a specific stage.
 */
public class StageLogActionResolver extends ParameterisedActionResolver
{
    private String stage;

    public StageLogActionResolver(String stage)
    {
        super("tailBuildLog");
        this.stage = stage;
    }

    public Map<String, String> getParameters()
    {
        Map<String, String> params = new HashMap<String, String>(1);
        params.put("stageName", stage);
        return params;
    }
}
