package com.zutubi.pulse.cleanup.config;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.tove.ColumnFormatter;

import java.util.List;

/**
 *
 *
 */
public class CleanupStateColumnFormatter implements ColumnFormatter
{
    public String format(Object obj)
    {
        List<ResultState> states = (List<ResultState>) obj;

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
