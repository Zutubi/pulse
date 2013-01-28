package com.zutubi.pulse.core.commands.msbuild;

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.resources.api.*;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Finds installed versions of the .Net framework and returns MsBuild resources
 * for them.
 */
public class MsBuildResourceLocator implements ResourceLocator
{
    private static final Logger LOG = Logger.getLogger(MsBuildResourceLocator.class);

    public List<ResourceConfiguration> locate()
    {
        if (!SystemUtils.IS_WINDOWS)
        {
            return Collections.emptyList();
        }

        String windowsDir = System.getProperty("SystemRoot");
        if (windowsDir == null)
        {
            windowsDir = guessWindowsDir();
            if( windowsDir == null)
            {
                return Collections.emptyList();
            }
        }

        windowsDir = FileSystemUtils.normaliseSeparators(windowsDir);
        ResourceLocator locator = new CompositeResourceLocator(getLocator(windowsDir, true), getLocator(windowsDir, false));
        return locator.locate();
    }

    private FileSystemResourceLocator getLocator(String windowsDir, final boolean bit64)
    {
        String dotNetPattern = StringUtils.join(PathPatternFileLocator.SEPARATOR, windowsDir, "Microsoft.NET", bit64 ? "Framework64" : "Framework", PathPatternFileLocator.WILDCARD);
        FileLocator fileLocator = 
                new ReversingFileLocator(
                        new SortingFileLocator(
                                new FilteringFileLocator(
                                        new DirectoryFilteringFileLocator(
                                                new PathPatternFileLocator(dotNetPattern)
                                        ),
                                        new DotNetInstallPredicate()
                                ),
                                new VersionComparator()
                        )
                );

        return new FileSystemResourceLocator(fileLocator, new FileSystemResourceBuilder()
        {
            public ResourceConfiguration buildResource(File home)
            {
                try
                {
                    ResourceConfiguration resource = new ResourceConfiguration("msbuild");
                    String versionName = home.getName().substring(1);
                    if (bit64)
                    {
                        versionName += " (64-bit)";
                    }
                    ResourceVersionConfiguration version = new ResourceVersionConfiguration(versionName);
                    resource.addVersion(version);
                    resource.setDefaultVersion(version.getValue());
                    version.addProperty(new ResourcePropertyConfiguration("msbuild.bin", FileSystemUtils.normaliseSeparators(getMsBuildBinary(home).getCanonicalPath()), false, false));
                    return resource;
                }
                catch (Exception e)
                {
                    LOG.warning("Error discovering msbuild resource: " + e.getMessage(), e);
                    return null;
                }
            }
        });
    }

    private File getMsBuildBinary(File home)
    {
        return new File(home, "MsBuild.exe");
    }

    private String guessWindowsDir()
    {
        // Look for the most common cases first
        File f = new File("c:/Windows");
        if (f.isDirectory())
        {
            return f.getAbsolutePath();
        }

        f = new File("c:/WINNT");
        if (f.isDirectory())
        {
            return f.getAbsolutePath();
        }

        // Try on any file system root.
        FileLocator locater = new PathPatternFileLocator("Windows", "WINNT");
        List<File> list = new LinkedList<File>(locater.locate());
        if (list.size() > 0)
        {
            // Prefer earlier roots (drive letter earlier in the alphabet)
            final Sort.StringComparator stringComparator = new Sort.StringComparator();
            Collections.sort(list, new Comparator<File>()
            {
                public int compare(File o1, File o2)
                {
                    return stringComparator.compare(o1.getAbsolutePath(), o2.getAbsolutePath());
                }
            });

            return list.get(0).getAbsolutePath();
        }

        return null;
    }

    private class DotNetInstallPredicate implements Predicate<File>
    {
        public boolean apply(File file)
        {
            return file.getName().startsWith("v") && getMsBuildBinary(file).isFile();
        }
    }

    private static class VersionComparator implements Comparator<File>
    {
        public int compare(File o1, File o2)
        {
            try
            {
                String[] v1 = o1.getName().split("\\.");
                String[] v2 = o2.getName().split("\\.");

                for (int i = 0; i < v1.length && i < v2.length; i++)
                {
                    String c1 = v1[i];
                    String c2 = v2[i];

                    int i1 = Integer.parseInt(c1);
                    int i2 = Integer.parseInt(c2);

                    if (i1 != i2)
                    {
                        return i1 - i2;
                    }
                }

                return v1.length - v2.length;
            }
            catch (NumberFormatException e)
            {
                // Can't order these.
                return 0;
            }
        }
    }
}
