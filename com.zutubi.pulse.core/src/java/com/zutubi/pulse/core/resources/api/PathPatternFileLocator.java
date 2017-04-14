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

package com.zutubi.pulse.core.resources.api;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.concurrent.ConcurrentUtils;
import com.zutubi.util.io.FileSystem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A file locator that searches all file system roots for files that match
 * simple patterns.  The patterns are just paths that use forward slashes as
 * separators, and may include stars which will match any name.  Stars can only
 * be used to match whole path elements.  For example:
 * 
 * <ul>
 *     <li>a/fixed/path</li>
 *     <li>Windows/Microsoft.Net/Framework/*</li>
 * </ul>
 * 
 * Note that because listing file system roots is expensive, this locator
 * limits the time it will allow for the operation.  If the timeout is reached
 * incomplete results may be returned.
 */
public class PathPatternFileLocator implements FileLocator
{
    /**
     * Used to separate path elements in patterns.
     */
    public static String SEPARATOR = "/";
    /**
     * Used to specify a path element that matches any string.
     */
    public static String WILDCARD = "*";

    private static final int ROOT_LISTING_TIMEOUT_SECONDS = 5;
    private static final AllFilesFilenameFilter ALL_FILES_FILENAME_FILTER = new AllFilesFilenameFilter();

    private String[] patterns;
    private FileSystem fileSystem = new FileSystem();

    /**
     * Creates a locator that combines the result of matching all patterns.
     * 
     * @param patterns the set of patterns to search for
     */
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

    private Collection<File> findPathsMatchingPattern(String pattern)
    {
        String[] elements = pattern.split(SEPARATOR);

        Collection<File> matching = null;
        for (String element : elements)
        {
            final FilenameFilter filter = getFilterForElement(element);

            Collection<File> matchesElement = new LinkedList<File>();
            if (matching == null)
            {
                List<File> roots = ConcurrentUtils.runWithTimeout(new Callable<List<File>>()
                {
                    public List<File> call() throws Exception
                    {
                        return Arrays.asList(fileSystem.listRoots());
                    }
                }, ROOT_LISTING_TIMEOUT_SECONDS, TimeUnit.SECONDS, Collections.<File>emptyList());

                matchesElement = Collections2.filter(roots, new Predicate<File>()
                {
                    public boolean apply(File file)
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

            matching = Lists.newArrayList(matchesElement);
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
