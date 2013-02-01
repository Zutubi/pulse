package com.zutubi.pulse.core.scm.api;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.io.FileSystemUtils;
import org.apache.tools.ant.types.selectors.SelectorUtils;

import java.io.File;
import java.util.List;

/**
 * A predicate that is satisfied if and only if the tested string passes
 * specified inclusion and exclusion filters.  To pass the predicate, a path
 * must match at least one inclusion and no exclusions.  As a special case, if
 * there are no inclusions only exclusions are applied (i.e. specifying no
 * inclusions is equivalent to having inclusions that match all paths).
 *
 * The filter path format uses ant selector formatting.
 * <ul>
 * <li>** - matches multiple path components, ie: this/path/is/matched.</li>
 * <li>*  - matches a single path component.</li>
 * <li>the rest is matched literally.</li>
 * </ul>
 */
public class FilterPathsPredicate implements Predicate<String>
{
    private List<String> includedPaths = null;
    private List<String> excludedPaths = null;

    public FilterPathsPredicate(List<String> includedPaths, List<String> excludedPaths)
    {
        Function<String, String> normaliseFunction = new Function<String, String>()
        {
            public String apply(String s)
            {
                return normalisePath(s);
            }
        };

        this.includedPaths = CollectionUtils.map(includedPaths, normaliseFunction);
        this.excludedPaths = CollectionUtils.map(excludedPaths, normaliseFunction);
    }

    public boolean apply(String path)
    {
        path = normalisePath(path);
        return isIncluded(path) && !isExcluded(path);
    }

    private boolean isIncluded(String path)
    {
        return includedPaths.isEmpty() || pathMatchesPatterns(path, includedPaths);
    }

    private boolean isExcluded(String path)
    {
       return pathMatchesPatterns(path, excludedPaths);
    }

    private boolean pathMatchesPatterns(String path, List<String> patterns)
    {
        for (String pattern: patterns)
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
                return true;
            }
        }

        return false;
    }

    private String normalisePath(String path)
    {
        return FileSystemUtils.localiseSeparators(path);
    }
}
