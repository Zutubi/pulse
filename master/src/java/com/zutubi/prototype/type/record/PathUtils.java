package com.zutubi.prototype.type.record;

import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Predicate;
import com.zutubi.pulse.util.StringUtils;

/**
 */
public class PathUtils
{
    private static final String SEPARATOR = "/";
    public static final String WILDCARD_ANY_ELEMENT = "*";

    public static String[] getPathElements(String path)
    {
        return getPathElements(path, false);
    }

    public static String[] getPathElements(String path, boolean allowEmpty)
    {
        String[] elements = path.split(SEPARATOR);
        if (!allowEmpty)
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
        if (elements.length == 0)
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

    public static String getParentPath(String path)
    {
        int i = path.lastIndexOf(SEPARATOR);
        if (i != -1)
        {
            return path.substring(0, i);
        }
        return null;
    }

    public static String getBasePath(String path)
    {
        int i = path.lastIndexOf(SEPARATOR);
        if (i != -1 && i < path.length())
        {
            return path.substring(i + 1);
        }
        return null;
    }

    public static String normalizePath(String path)
    {
        if (path.startsWith(SEPARATOR))
        {
            path = path.substring(1);
        }
        if (path.endsWith(SEPARATOR))
        {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String getPath(String... pathElements)
    {
        return StringUtils.join(SEPARATOR, true, true, pathElements);
    }

    public static boolean prefixMatches(String candidate, String prefix)
    {
        String[] candidateParts = getPathElements(candidate);
        String[] prefixParts = getPathElements(prefix);

        if(candidateParts.length < prefixParts.length)
        {
            return false;
        }
        
        for(int i = 0; i < prefixParts.length; i++)
        {
            if(!matches(candidateParts[i], prefixParts[i]))
            {
                return false;
            }
        }

        return true;
    }

    public static String stripMatchingPrefix(String path, String prefix)
    {
        String[] parts = getPathElements(path);
        String[] prefixParts = getPathElements(prefix);

        int i;
        for(i = 0; i < prefixParts.length; i++)
        {
            if(i >= parts.length || !matches(parts[i], prefixParts[i]))
            {
                break;
            }
        }

        String[] newParts = new String[parts.length - i];
        System.arraycopy(parts, i, newParts, 0, newParts.length);
        return getPath(newParts);
    }

    public static boolean matches(String pattern, String element)
    {
        return pattern.equals(WILDCARD_ANY_ELEMENT) || pattern.equals(element);
    }
}
