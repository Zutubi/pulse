package com.zutubi.pulse.master.velocity;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Sort;
import com.zutubi.util.io.FileSuffixFilter;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class to help deal with defined dependencies between javascript
 * files.
 *
 * This class does two things.
 * <ul>
 * <li>extracts the dependency details from the javascript files</li>
 * <li>sorts those dependencies in dependent order</li>
 * </ul>
 */
// Implementation note: this code has been extracted from the JavascriptDirective to
// make it easier to reuse in the ant task that compacts the javascript files into a single
// file.  SO, ANY CHANGES HERE NEED TO BE APPLIED TO THE ANT TASK AS WELL.
public class JavascriptDependencies
{
    private static final Logger LOG = Logger.getLogger(JavascriptDependencies.class);

    /**
     * Regex pattern for matching the dependency header on js files.
     */
    private static final Pattern PATTERN_DEPENDENCY_HEADER = Pattern.compile("[\\s]*//[\\s]*dependency[\\s]*:.*");

    private static final Comparator<String> STRING_COMPARATOR = new Sort.StringComparator();

    public static List<String> expandAndSortPaths(File jsRoot, List<String> jsPaths) throws IOException
    {
        Map<String, List<String>> directDependencies = new HashMap<String, List<String>>();
        expand(jsRoot, jsPaths, directDependencies);

        List<String> sortedPaths = new LinkedList<String>();
        while (directDependencies.size() > 0)
        {
            List<String> paths = new LinkedList<String>(directDependencies.keySet());
            
            // We can not rely on the internal ordering of a set, so we sort the paths to ensure
            // we produce a consistent result.  
            Collections.sort(paths, STRING_COMPARATOR);

            for (String path : paths)
            {
                if (directDependencies.get(path).size() == 0)
                {
                    sortedPaths.add(path);

                    directDependencies.remove(path);
                    List<String> linkedList = new LinkedList<String>(directDependencies.keySet());
                    for (String s : linkedList)
                    {
                        List<String> deps = directDependencies.get(s);
                        deps.remove(path);
                    }
                }
            }
            if (paths.size() == directDependencies.size())
            {
                // There is a dependency that has a dependency that is
                // not a path, meaning it doesn't exist.  Dump the remaining
                // dependency details to aid fixing the problem.
                for (String p : directDependencies.keySet())
                {
                    StringBuffer b = new StringBuffer();
                    b.append(p).append(" > ");
                    List<String> l = directDependencies.get(p);
                    String sep = "";
                    for (String s : l)
                    {
                        b.append(sep).append(s);
                        sep = ", ";
                    }
                    LOG.error(b.toString());
                }

                throw new IOException("Malformed dependency header.  See logs for details.");
            }
        }
        return sortedPaths;
    }

    /**
     * Expand and resolve the dependency details for the given paths.
     *
     * @param jsRoot        the base path for all javascript files
     * @param paths         the paths to be processed.
     * @param expanded  the resulting list of paths to dependencies map
     * @throws IOException  is thrown if there are problems reading any of the paths.
     */
    private static void expand(final File jsRoot, List<String> paths, Map<String, List<String>> expanded) throws IOException
    {
        for (String path: paths)
        {
            if (!expanded.containsKey(path))
            {
                final File file = new File(jsRoot, path);
                if (file.isFile())
                {
                    expandFile(jsRoot, expanded, file, path);
                }
                else if (file.isDirectory())
                {
                    expandDirectory(jsRoot, expanded, file);
                }
            }
        }
    }

    private static void expandDirectory(File jsRoot, Map<String, List<String>> expanded, File file) throws IOException
    {
        List<String> listing = new LinkedList<String>();
        File[] jsFiles = file.listFiles(new FileSuffixFilter(".js"));
        if (jsFiles != null)
        {
            for (File f : jsFiles)
            {
                if (f.isFile())
                {
                    listing.add(FileSystemUtils.relativePath(jsRoot, f));
                }
            }
        }
        expand(jsRoot, listing, expanded);
    }

    private static void expandFile(final File jsRoot, Map<String, List<String>> expanded, final File jsFile, String dependencyPath) throws IOException
    {
        List<String> dependencies = readDependencyHeader(jsFile);
        List<String> resolvedDependencies = CollectionUtils.map(dependencies, new Mapping<String, String>()
        {
            public String map(String dependency)
            {
                return resolvePath(jsRoot, jsFile, dependency);
            }
        });
        expanded.put(dependencyPath, resolvedDependencies);
        expand(jsRoot, resolvedDependencies, expanded);
    }

    /**
     * Resolve the dependency string into a path relative to the js root.
     * @param jsRoot                the directory to which the result will be relative to
     * @param jsFile                    the file that the dependency applies to
     * @param dependency        the relative dependency
     *
     * @return  path relative to the jsRoot
     */
    private static String resolvePath(File jsRoot, File jsFile, String dependency)
    {
        // make use of getCanonicalFile() to do the conversions.
        try
        {
            if (dependency.startsWith("./") || dependency.startsWith("../"))
            {
                File jsParent = jsFile.getParentFile().getCanonicalFile();
                File target = new File(jsParent, dependency).getCanonicalFile();
                return FileSystemUtils.relativePath(jsRoot.getCanonicalFile(), target);
            }
            else // already relative to the root.
            {
                return dependency;
            }
        }
        catch (IOException e)
        {
            return e.getMessage();
        }
    }

    /**
     * Read the header of the javascript files, extracting the list of dependencies.
     *
     * @param file  the javascript file
     * @return  a list of dependencies, or an empty list if no dependencies are
     * defined.
     *
     * @throws IOException if there are any problems reading the file.
     */
    private static List<String> readDependencyHeader(File file) throws IOException
    {
        List<String> dependencies = new LinkedList<String>();

        InputStream input = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        try
        {
            while (true)
            {
                String line = reader.readLine();
                if (line == null)
                {
                    break;
                }
                
                Matcher matcher = PATTERN_DEPENDENCY_HEADER.matcher(line);
                if (!matcher.matches())
                {
                    break;
                }
                
                line = line.substring(line.indexOf(":") + 1);
                StringTokenizer tokens = new StringTokenizer(line, ",", false);
                while (tokens.hasMoreTokens())
                {
                    dependencies.add(tokens.nextToken().trim());
                }
            }
        }
        finally
        {
            IOUtils.close(reader);
        }

        return dependencies;
    }

}
