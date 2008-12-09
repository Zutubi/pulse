package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.tove.annotations.Formatter;

import java.util.List;

/**
 *
 *
 */
public class CleanupStateColumnFormatter implements Formatter<List<ResultState>>
{
    public String format(List<ResultState> states)
    {
        if(states == null || states.size() == 0)
        {
            return "any";
        }
        else
        {
            String result = "";

            for(ResultState state: states)
            {
                if(result.length() > 0)
                {
                    result += ", ";
                }

                result += state.getPrettyString();
            }
            return result;
        }
    }
}
