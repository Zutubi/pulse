package com.zutubi.pulse.core.scm.api;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import org.apache.tools.ant.types.selectors.SelectorUtils;

import java.io.File;
import java.util.List;

/**
 */
public class ExcludePathFilter implements PathFilter
{
    private List<String> excludedPaths = null;

    public ExcludePathFilter(List<String> excludedPaths)
    {
        this.excludedPaths = CollectionUtils.map(excludedPaths, new Mapping<String, String>()
        {
            public String map(String s)
            {
                return normalisePath(s);
            }
        });
    }

    public boolean accept(String path)
    {
        path = normalisePath(path);

        for (String pattern : excludedPaths)
        {
            // The Ant selector will only match a path starting with the
            // file separator if the pattern also starts with the file
            // separator.  This is sensible enough in Ant context, but
            // here the SCMs can report paths like "/project/trunk/foo"
            // or "//depot/bar".  A user will probably expect **/... to
            // match in these cases, so we treat it as a special case.
            String matchPath = path;
            if (pattern.startsWith("**"))
            {
                while (matchPath.startsWith(File.separator))
                {
                    matchPath = matchPath.substring(1);
                }
            }

            if (SelectorUtils.matchPath(pattern, matchPath))
            {
                return false;
            }
        }
        return true;
    }

    private String normalisePath(String path)
    {
        return path.replace('/', File.separatorChar).replace('\\', File.separatorChar);
    }
}
