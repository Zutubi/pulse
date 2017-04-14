/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
