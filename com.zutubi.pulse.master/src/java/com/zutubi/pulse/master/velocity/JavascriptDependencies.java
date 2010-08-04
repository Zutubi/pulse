package com.zutubi.pulse.master.velocity;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class to help deal with defined dependencies between javascript
 * files.
 */
public class JavascriptDependencies
{
    /**
     * Regex pattern for matching the dependency header on js files.
     */
    private static final Pattern PATTERN_DEPENDENCY_HEADER = Pattern.compile("[\\s]*//[\\s]*dependency[\\s]*:.*");

    public static List<String> expandAndSortPaths(File jsRoot, List<String> jsPaths) throws IOException
    {
        Map<String, List<String>> directDependencies = new HashMap<String, List<String>>();
        expand(jsRoot, jsPaths, directDependencies);

        List<String> sortedPaths = new LinkedList<String>();
        while (directDependencies.size() > 0)
        {
            List<String> paths = new LinkedList<String>(directDependencies.keySet());
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
        }
        return sortedPaths;
    }

    private static void expand(File jsRoot, List<String> paths, Map<String, List<String>> expanded) throws IOException
    {
        for (String path: paths)
        {
            if (!expanded.containsKey(path))
            {
                List<String> dependencies = readDependencyHeader(new File(jsRoot, path));
                expanded.put(path, dependencies);

                expand(jsRoot, dependencies, expanded);
            }
        }
    }

    private static List<String> readDependencyHeader(File file) throws IOException
    {
        List<String> dependencies = new LinkedList<String>();

        InputStream input = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        try
        {
            String line = reader.readLine();
            Matcher matcher = PATTERN_DEPENDENCY_HEADER.matcher(line);
            while (matcher.matches())
            {
                line = line.substring(line.indexOf(":") + 1);
                StringTokenizer tokens = new StringTokenizer(line, ",", false);
                while (tokens.hasMoreTokens())
                {
                    dependencies.add(tokens.nextToken().trim());
                }

                line = reader.readLine();
                matcher = PATTERN_DEPENDENCY_HEADER.matcher(line);
            }
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }

        return dependencies;
    }

}
