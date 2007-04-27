package com.zutubi.pulse.cleanup.config;

import com.zutubi.prototype.Formatter;
import com.zutubi.pulse.core.model.ResultState;

import java.util.List;

/**
 *
 *
 */
public class CleanupConfigurationFormatter implements Formatter<CleanupConfiguration>
{
    public String format(CleanupConfiguration config)
    {
        // we really want to be able to display multiple cells here, rendering enough information
        // so that a user can clearly see what the cleanup rule is doing.

        return config.getName() + String.format("(%s, %s)", getPrettyStateNames(config), getPrettyWhen(config));
    }

    protected String getPrettyStateNames(CleanupConfiguration config)
    {
        List<ResultState> states = config.getStates();
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

    protected String getPrettyWhen(CleanupConfiguration config)
    {
        if(config.getUnit() == CleanupUnit.BUILDS)
        {
            return config.getRetain() + " builds";
        }
        else
        {
            return  config.getRetain() + " days";
        }
    }
}
