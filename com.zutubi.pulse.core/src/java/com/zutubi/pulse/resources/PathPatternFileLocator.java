package com.zutubi.pulse.resources;

import com.zutubi.pulse.util.FileSystem;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.SystemUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class PathPatternFileLocator implements FileLocator
{
    public static String SEPARATOR = "/";
    public static String WILDCARD = "*";

    private static final AllFilesFilenameFilter ALL_FILES_FILENAME_FILTER = new AllFilesFilenameFilter();

    private String[] patterns;
    private FileSystem fileSystem = new FileSystem();

    public PathPatternFileLocator(String... patterns)
    {
        this.patterns = patterns;
    }

    public List<File> locate()
    {
        List<File> result = new LinkedList<File>();
        for (String pattern : patterns)
        {
            result.addAll(findPathsMatchingPattern(pattern));
        }

        return result;
    }

    private List<File> findPathsMatchingPattern(String pattern)
    {
        String[] elements = pattern.split(SEPARATOR);

        List<File> matching = null;
        for (String element : elements)
        {
            final FilenameFilter filter = getFilterForElement(element);

            List<File> matchesElement = new LinkedList<File>();
            if (matching == null)
            {
                matchesElement = CollectionUtils.filter(Arrays.asList(fileSystem.listRoots()), new Predicate<File>()
                {
                    public boolean satisfied(File file)
                    {
                        String name = file.getAbsolutePath();
                        if (name.endsWith(File.separator))
                        {
                            name = name.substring(0, name.length() - 1);
                        }

                        return filter.accept(file, name);
                    }
                });
            }
            else
            {
                for (File f : matching)
                {
                    matchesElement.addAll(Arrays.asList(fileSystem.listFiles(f, filter)));
                }
            }

            matching = matchesElement;
        }
        return matching;
    }

    private FilenameFilter getFilterForElement(final String piece)
    {
        if (WILDCARD.equals(piece))
        {
            return ALL_FILES_FILENAME_FILTER;
        }
        else
        {
            return new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    if (SystemUtils.IS_WINDOWS)
                    {
                        return piece.equalsIgnoreCase(name);
                    }
                    else
                    {
                        return piece.equals(name);
                    }
                }
            };
        }
    }

    public void setFileSystem(FileSystem fileSystem)
    {
        this.fileSystem = fileSystem;
    }

    /**
     * A filter that accepts all files.
     */
    private static class AllFilesFilenameFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return true;
        }
    }
}
