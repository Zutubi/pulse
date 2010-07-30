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
 * </code>
 *
 * NOTE: This directive will not pick up javascript files that are dropped into the CONTENT_ROOT/js
 * directory after Pulse has rendered the first page.
 */
public class JavascriptDirective extends AbstractDirective
{
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
     */
    private static final Map<String, String> cache = new HashMap<String, String>();

    public JavascriptDirective()
    {
        SpringComponentContext.autowire(this);
    }

    public String getName()
    {
        return "javascript";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        String base = configurationManager.getSystemConfig().getContextPathNormalised();

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

        return true;
    }

    private String generateContent(String base) throws IOException
    {
        StringBuffer content = new StringBuffer();

        File contentRoot = configurationManager.getSystemPaths().getContentRoot();

        List<File> jsFiles = FileSystemUtils.filter(
                new File(contentRoot, "js"),
                new FileSuffixPredicate(".js")
        );

        Map<String, List<String>> directDependencies = new HashMap<String, List<String>>();
        for (File jsFile : jsFiles)
        {
            String path = FileSystemUtils.relativePath(contentRoot, jsFile);
            directDependencies.put(path, readDependencyHeader(jsFile));
        }

        // Sort the js files according to there dependencies..
        List<String> orderedPaths = new LinkedList<String>();
        while (directDependencies.size() > 0)
        {
            List<String> paths = new LinkedList<String>(directDependencies.keySet());
            for (String path : paths)
            {
                if (directDependencies.get(path).size() == 0)
                {
                    orderedPaths.add(path);

                    // remove all references to path from the direct dependencies map.
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

        for (String path : orderedPaths)
        {
            content.append("<script type=\"text/javascript\" src=\"");
            content.append(base).append("/").append(path);
            content.append("?ver=").append(version);
            content.append("\"></script>\n");
        }
        
        return content.toString();
    }

    private List<String> readDependencyHeader(File file) throws IOException
    {
        // The expected header format is as follows:
        // dependency:  comma,separated,list
        // dependency:  of,dependencies
        // .......

        List<String> dependencies = new LinkedList<String>();

        InputStream input = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        try
        {
            String tag = "// dependency:";
            String line = reader.readLine();
            while (line.startsWith(tag))
            {
                StringTokenizer tokens = new StringTokenizer(line.substring(tag.length()), " ,", false);
                while (tokens.hasMoreTokens())
                {
                    dependencies.add(tokens.nextToken());
                }
                line = reader.readLine();
            }
        }
        finally
        {
            IOUtils.close(reader);
        }

        return dependencies;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
