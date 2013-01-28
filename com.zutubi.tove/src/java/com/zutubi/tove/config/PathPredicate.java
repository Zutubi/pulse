package com.zutubi.tove.config;

import com.google.common.base.Predicate;
import com.zutubi.events.Event;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.type.record.PathUtils;

/**
 * An event predicate that is satisfied by {@link com.zutubi.tove.config.events.ConfigurationEvent}s
 * with a path that matches one of a set of patterns.
 */
public class PathPredicate implements Predicate<Event>
{
    private boolean includeChildPaths;
    private String[] patterns;

    public PathPredicate(boolean includeChildPaths, String... patterns)
    {
        this.includeChildPaths = includeChildPaths;
        this.patterns = patterns;
    }

    public boolean apply(Event event)
    {
        if(event instanceof ConfigurationEvent)
        {
            final String path = ((ConfigurationEvent) event).getInstance().getConfigurationPath();
            for(String pattern: patterns)
            {
                if(includeChildPaths)
                {
                    if(PathUtils.prefixPatternMatchesPath(pattern, path))
                    {
                        return true;
                    }
                }
                else
                {
                    if(PathUtils.pathMatches(pattern, path))
                    {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
}
