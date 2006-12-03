package com.zutubi.pulse.scm;

import org.apache.tools.ant.types.selectors.SelectorUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class ScmFilepathFilter implements FilepathFilter
{
    private List<String> excludedPaths;

    public ScmFilepathFilter(List<String> excludedPaths)
    {
        this.excludedPaths = new ArrayList<String>(excludedPaths.size());
        for(String pattern: excludedPaths)
        {
            this.excludedPaths.add(transformPath(pattern));
        }
    }

    public boolean accept(String path)
    {
        path = transformPath(path);

        if (excludedPaths != null)
        {
            for (String pattern : excludedPaths)
            {
                // The Ant selector will only match a path starting with the
                // file separator if the pattern also starts with the file
                // separator.  This is sensible enough in Ant context, but
                // here the SCMs can report paths like "/project/trunk/foo"
                // or "//depot/bar".  A user will probably expect **/... to
                // match in these cases, so we treat it as a special case.
                String matchPath = path;
                if(pattern.startsWith("**"))
                {
                    while(matchPath.startsWith(File.separator))
                    {
                        matchPath = matchPath.substring(1);
                    }
                }
                
                if (SelectorUtils.matchPath(pattern, matchPath))
                {
                    return false;
                }
            }
        }
        return true;
    }

    private String transformPath(String path)
    {
        return path.replace('/', File.separatorChar).replace('\\', File.separatorChar);
    }
}
