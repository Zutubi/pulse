package com.zutubi.pulse.core.scm.api;

import com.google.common.base.Predicate;
import com.zutubi.util.io.FileSystemUtils;

/**
 * A predicate that rejects all path strings that do not have the
 * specified path prefix.
 *
 * All strings are normalised to allow separator chars to be handled
 * predictably.
 */
public class PrefixPathFilter implements Predicate<String>
{
    /**
     * The required prefix of all accepted paths.
     */
    private String prefix;

    public PrefixPathFilter(String prefix)
    {
        this.prefix = normalisePath(prefix);
    }

    public boolean apply(String path)
    {
        String normalisedPath = normalisePath(path);
        return normalisedPath.startsWith(prefix);
    }

    private String normalisePath(String path)
    {
        return FileSystemUtils.localiseSeparators(path);
    }
}
