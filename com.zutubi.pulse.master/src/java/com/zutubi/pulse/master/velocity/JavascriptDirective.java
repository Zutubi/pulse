package com.zutubi.pulse.master.velocity;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.FileSuffixPredicate;
import com.zutubi.util.io.IOUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Generate html <script/> references for the javascript files located in
 * CONTENT_ROOT/js
 *
 * The generated output is of the following form:
 *
 * <script type="text/javascript" src="${base}/js/scriptB.js?ver=x.x.xx"></script>
 *
 * where base is the normalised context path, and x.x.xx is the version string.
 *
 * The order of the script tags can be controlled by adding dependency headers to the
 * javascript files.  The headers should be in comments at the very top of the file, and
 * take the following form:
 *
 * // dependency: a,comma, separated, list
 * // dependency: that, can, span, multiple, lines
 *
 * Usage of this directive within a velocity template looks as follows:
 * <code>
 *     #javascript()
 *         fileA.js
 *         fileB.js 
 *     #end
 * </code>
 *
 * During development, this directive will render the js files contained within the body of the
 * directive.  During production, it will default to loading the javascript files available on the
 * file system.
 */
public class JavascriptDirective extends AbstractDirective
{
    /**
     * A system flag that is true when we are running in development, false otherwise.
     */
    private static final boolean IS_DEVELOPMENT = Boolean.getBoolean("development");
    
    private static final String DIRECTIVE_NAME = "javascript";

    /**
     * Regex pattern for matching the dependency header on js files.
     */
    private static final Pattern PATTERN_DEPENDENCY_HEADER = Pattern.compile("[\\s]*//[\\s]*dependency[\\s]*:.*");

    /**
     * The verison string to be appended to each of the source references.  It will ensure that
     * caches don't get js files confused between releases.
     */
    private static final String version = Version.getVersion().getVersionNumber();

    private MasterConfigurationManager configurationManager;

    /**
     * A cache of generated output.  The key is the base string, the value is the generated html.
     *
     * The output from this directive is the same (except for possible changes to the base).
     *
     * This cache is only used when {@link #IS_DEVELOPMENT} is false.
     */
    private static final Map<String, String> cache = new HashMap<String, String>();

    private static final String JS_ROOT = "js";

    public JavascriptDirective()
    {
        // Until we can manage the creation of the directives, we need to autowire them here.
        SpringComponentContext.autowire(this);
    }

    public String getName()
    {
        return DIRECTIVE_NAME;
    }

    public int getType()
    {
        return BLOCK;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        String base = configurationManager.getSystemConfig().getContextPathNormalised();

        if (IS_DEVELOPMENT)
        {
            // in development, we generate the content fresh each time as
            // we are dealing with the individual pre-processed js files.
            writer.write(generateContent(base, context, node));
        }
        else
        {
            synchronized (cache)
            {
                if (!cache.containsKey(base))
                {
                    // generate the content.
                    String content = generateContent(base);
                    cache.put(base, content);
                }
            }
            writer.write(cache.get(base));
        }

        return true;
    }

     // In development, we generate content based on the strings contained
     // within the body of the directive.
    private String generateContent(String base, InternalContextAdapter context, Node node) throws MethodInvocationException, IOException, ResourceNotFoundException, ParseErrorException
    {
        String bodyContent = extractBodyContext(node, context);

        List<String> requestedPaths = new LinkedList<String>();
        StringTokenizer tokens = new StringTokenizer(bodyContent, "\n", false);
        while (tokens.hasMoreTokens())
        {
            String requestedPath = tokens.nextToken().trim();
            if (requestedPath.length() > 0)
            {
                requestedPaths.add(requestedPath);
            }
        }

        File contentRoot = configurationManager.getSystemPaths().getContentRoot();
        File jsRoot = new File(contentRoot, JS_ROOT);

        return generateContent(base, jsRoot, requestedPaths);
    }

    // In production, we generate content based on the js files on the file system.
    private String generateContent(String base) throws IOException
    {
        File contentRoot = configurationManager.getSystemPaths().getContentRoot();
        File jsRoot = new File(contentRoot, JS_ROOT);

        List<String> jsPaths = new LinkedList<String>();
        List<File> jsFiles = FileSystemUtils.filter(jsRoot,new FileSuffixPredicate(".js"));
        for (File jsFile : jsFiles)
        {
            jsPaths.add(FileSystemUtils.relativePath(jsRoot, jsFile));
        }

        return generateContent(base, jsRoot, jsPaths);
    }

    private String generateContent(String base, File jsRoot, List<String> jsPaths) throws IOException
    {
        StringBuffer content = new StringBuffer();

        List<String> sortedPaths = expandAndSortPaths(jsRoot, jsPaths);

        // ensure that the requested paths exist and are files.
        for (String requestedPath : sortedPaths)
        {
            ensureIsFile(jsRoot, requestedPath);
        }

        for (String path : sortedPaths)
        {
            content.append("<script type=\"text/javascript\" src=\"");
            content.append(base).append("/"+JS_ROOT+"/").append(path);
            content.append("?ver=").append(version);
            content.append("\"> </script>\n");
        }

        return content.toString();
    }

    private List<String> expandAndSortPaths(File jsRoot, List<String> jsPaths) throws IOException
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

    private void expand(File jsRoot, List<String> paths, Map<String, List<String>> expanded) throws IOException
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

    private List<String> readDependencyHeader(File file) throws IOException
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
            IOUtils.close(reader);
        }

        return dependencies;
    }

    private void ensureIsFile(File base, String path) throws IOException
    {
        File file = new File(base, path);
        if (!file.isFile())
        {
            throw new IOException("Unknown file: " + path);
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
