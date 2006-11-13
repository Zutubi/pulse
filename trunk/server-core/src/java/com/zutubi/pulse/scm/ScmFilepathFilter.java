package com.zutubi.pulse.scm;

import org.apache.tools.ant.types.selectors.SelectorUtils;

import java.util.List;

/**
 * <class-comment/>
 */
public class ScmFilepathFilter implements FilepathFilter
{
    private List<String> excludedPaths;

    public ScmFilepathFilter(List<String> excludedPaths)
    {
        this.excludedPaths = excludedPaths;
    }

    public boolean accept(String path)
    {
        if (excludedPaths != null)
        {
            for (String pattern : excludedPaths)
            {
                if (SelectorUtils.matchPath(pattern, path))
                {
                    return false;
                }
            }
        }
        return true;
    }
}
