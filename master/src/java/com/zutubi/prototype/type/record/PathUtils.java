package com.zutubi.prototype.type.record;

import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Predicate;
import com.zutubi.pulse.util.StringUtils;

/**
 */
public class PathUtils
{
    private static final String SEPARATOR = "/";

    public static String[] getPathElements(String path)
    {
        return getPathElements(path, false);
    }

    public static String[] getPathElements(String path, boolean allowEmpty)
    {
        String[] elements = path.split(SEPARATOR);
        if(!allowEmpty)
        {
            elements = CollectionUtils.filterToArray(elements, new Predicate<String>()
            {
                public boolean satisfied(String s)
                {
                    return s.length() > 0;
                }
            });
        }

        return elements;
    }

    public static String[] getParentPathElements(String path)
    {
        return getParentPathElements(path, false);
    }

    private static String[] getParentPathElements(String path, boolean allowEmpty)
    {
        String[] elements = getPathElements(path, allowEmpty);
        if(elements.length == 0)
        {
            return null;
        }
        else
        {
            return getParentPathElements(elements);
        }
    }

    public static String[] getParentPathElements(String[] elements)
    {
        String[] parent = new String[elements.length - 1];
        System.arraycopy(elements, 0, parent, 0, elements.length - 1);
        return parent;
    }

    public static String getPath(String[] pathElements)
    {
        return StringUtils.join(SEPARATOR, pathElements);
    }
}
