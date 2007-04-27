package com.zutubi.prototype.config;

import com.zutubi.pulse.events.Event;
import com.zutubi.util.Predicate;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.type.record.PathUtils;

/**
 * An event predicate that is satisfied by {@link com.zutubi.prototype.config.events.ConfigurationEvent}s
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

    public boolean satisfied(Event event)
    {
        if(event instanceof ConfigurationEvent)
        {
            final String path = ((ConfigurationEvent) event).getPath();
            for(String pattern: patterns)
            {
                if(includeChildPaths)
                {
                    if(PathUtils.prefixMatches(pattern, path))
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
