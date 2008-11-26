package com.zutubi.pulse.master.cleanup.config;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.tove.table.ColumnFormatter;

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
