package com.zutubi.pulse.core.resources;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.*;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 */
public class MsBuildResourceLocator implements ResourceLocator
{
    private static final Logger LOG = Logger.getLogger(MsBuildResourceLocator.class);

    public List<Resource> locate()
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
        String dotNetPattern = StringUtils.join(PathPatternFileLocator.SEPARATOR, windowsDir, "Microsoft.NET", "Framework", PathPatternFileLocator.WILDCARD);
        FileLocator fileLocator = new ReversingFileLocator(new SortingFileLocator(new FilteringFileLocator(new DirectoryFilteringFileLocator(new PathPatternFileLocator(dotNetPattern)), new DotNetInstallPredicate()), new VersionComparator()));

        FileSystemResourceLocator locator = new FileSystemResourceLocator(fileLocator, new ResourceBuilder()
        {
            public Resource buildResource(File home)
            {
                try
                {
                    Resource resource = new Resource("msbuild");
                    ResourceVersion version = new ResourceVersion(home.getName().substring(1));
                    resource.add(version);
                    resource.setDefaultVersion(version.getValue());
                    version.addProperty(new ResourceProperty("msbuild.bin", FileSystemUtils.normaliseSeparators(getMsBuildBinary(home).getCanonicalPath()), false, false, false));
                    return resource;
                }
                catch (Exception e)
                {
                    LOG.warning("Error discovering msbuild resource: " + e.getMessage(), e);
                    return null;
                }
            }
        });

        return locator.locate();
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
        List<File> list = locater.locate();
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
        public boolean satisfied(File file)
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
                String[] v2 = o1.getName().split("\\.");

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
