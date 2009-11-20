package com.zutubi.tove.type.record;

import com.zutubi.util.StringUtils;

/**
 */
public class PathUtils
{
    public static final char SEPARATOR_CHAR = '/';
    public static final String SEPARATOR = "/";
    public static final String WILDCARD_ANY_ELEMENT = "*";

    public static String[] getPathElements(String path)
    {
        return getPathElements(path, false);
    }

    public static String[] getPathElements(String path, boolean allowEmpty)
    {
        if (path == null)
        {
            return new String[0];
        }

        return StringUtils.split(path, SEPARATOR_CHAR, !allowEmpty);
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
        if (elements.length == 0)
        {
            return null;
        }
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

    public static String getBaseName(String path)
    {
        int i = path.lastIndexOf(SEPARATOR);
        if (i != -1)
        {
            return path.substring(i + 1);
        }
        return path;
    }

    public static String normalisePath(String path)
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
        return StringUtils.join(SEPARATOR_CHAR, true, true, pathElements);
    }

    public static String getPath(int beginIndex, String... pathElements)
    {
        if(beginIndex == 0)
        {
            return getPath(pathElements);
        }
        else if(beginIndex >= pathElements.length)
        {
            return "";
        }

        String [] newElements = new String[pathElements.length - beginIndex];
        System.arraycopy(pathElements, beginIndex, newElements, 0, newElements.length);
        return getPath(newElements);
    }

    public static String getPath(int beginIndex, int endIndex, String... pathElements)
    {
        if(endIndex >= pathElements.length)
        {
            return getPath(beginIndex, pathElements);
        }
        else if(beginIndex >= endIndex)
        {
            return "";
        }

        String [] newElements = new String[endIndex - beginIndex];
        System.arraycopy(pathElements, beginIndex, newElements, 0, newElements.length);
        return getPath(newElements);
    }

    public static boolean pathMatches(String pattern, String path)
    {
        String[] patternParts = getPathElements(pattern);
        String[] pathParts = getPathElements(path);

        if(patternParts.length != pathParts.length)
        {
            return false;
        }

        for(int i = 0; i < pathParts.length; i++)
        {
            if(!elementMatches(patternParts[i], pathParts[i]))
            {
                return false;
            }
        }

        return true;
    }

    public static boolean prefixMatchesPathPattern(String pattern, String prefix)
    {
        String[] patternParts = getPathElements(pattern);
        String[] prefixParts = getPathElements(prefix);

        if(patternParts.length < prefixParts.length)
        {
            return false;
        }

        for(int i = 0; i < prefixParts.length; i++)
        {
            if(!elementMatches(patternParts[i], prefixParts[i]))
            {
                return false;
            }
        }

        return true;
    }

    public static boolean prefixPatternMatchesPath(String prefixPattern, String path)
    {
        String[] pathParts = getPathElements(path);
        String[] patternParts = getPathElements(prefixPattern);

        if(pathParts.length < patternParts.length)
        {
            return false;
        }

        for(int i = 0; i < patternParts.length; i++)
        {
            if(!elementMatches(patternParts[i], pathParts[i]))
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
            if(i >= parts.length || !elementMatches(parts[i], prefixParts[i]))
            {
                break;
            }
        }

        String[] newParts = new String[parts.length - i];
        System.arraycopy(parts, i, newParts, 0, newParts.length);
        return getPath(newParts);
    }

    public static boolean elementMatches(String pattern, String element)
    {
        return pattern.equals(WILDCARD_ANY_ELEMENT) || pattern.equals(element);
    }

    public static String getPrefix(String path, int elements)
    {
        return getPath(0, elements, getPathElements(path));
    }

    /**
     * Gets the end part of the path by trimming off beginIndex elements from
     * the start.  If there are less than beginIndex elements, the empty string
     * is returned.
     *
     * @param path       path to retrieve the suffix of
     * @param beginIndex index of the first path element to return
     * @return the given string with beginIndex path elements stripped from the
     *         front
     */
    public static String getSuffix(String path, int beginIndex)
    {
        return getPath(beginIndex, getPathElements(path));
    }

    /**
     * Gets the specified element of a path.  If the specified index is beyond
     * the end of the path, the empty string is returned.
     *
     * @param path  path to retrieve the element from
     * @param index zero-based index of the element to retrieve
     * @return the index'th element of the path, or the empty string if there
     *         is no such element
     */
    public static String getElement(String path, int index)
    {
        return getPath(index, index + 1, getPathElements(path));
    }
}
