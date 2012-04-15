package com.zutubi.pulse.acceptance;

import com.sun.org.apache.bcel.internal.classfile.*;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.config.Config;
import com.zutubi.util.config.FileConfig;
import com.zutubi.util.config.ReadOnlyConfig;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class AcceptanceTestUtils
{
    /**
     * The host portion of the master URL.
     */
    public static final String MASTER_HOST = "localhost";
    
    /**
     * The acceptance test system property for the built pulse package.
     */
    protected static final String PROPERTY_PULSE_PACKAGE = "pulse.package";

    /**
     * The acceptance test system property for the built agent package.
     */
    protected static final String PROPERTY_AGENT_PACKAGE = "agent.package";

    /**
     * The acceptance test system property for the built dev package.
     */
    protected static final String PROPERTY_DEV_PACKAGE = "dev.package";
    
    /**
     * The acceptance test system property for the pulse startup port.
     */
    public static final String PROPERTY_PULSE_PORT = "pulse.port";

    /**
     * The acceptance test system property for the agent startup port.
     */
    public static final String PROPERTY_AGENT_PORT = "agent.port";

    /**
     * The web ui context path that will be used during acceptance testing. 
     */
    public static final String PROPERTY_PULSE_CONTEXT = "pulse.context.path";

    public static final String PROPERTY_WORK_DIR = "work.dir";

    /**
     * Id of the plugin which can be used to make test-specific plugins with
     * {@link #createTestPlugin(java.io.File, String, String)}. 
     */
    public static final String PLUGIN_ID_TEST = "com.zutubi.pulse.core.postprocessors.test";
    
    /**
     * The credentials for the admin user.
     */
    public static final UsernamePasswordCredentials ADMIN_CREDENTIALS = new UsernamePasswordCredentials("admin", "admin");

    public static int getPulsePort()
    {
        return Integer.getInteger(PROPERTY_PULSE_PORT, 8080);
    }
    
    public static void setPulsePort(int port)
    {
        System.setProperty(PROPERTY_PULSE_PORT, Integer.toString(port));
    }

    public static int getAgentPort()
    {
        return Integer.getInteger(PROPERTY_AGENT_PORT, 8890);
    }
    
    public static String getPulseUrl()
    {
        return getPulseUrl(getPulsePort());
    }

    public static String getContextPath()
    {
        String path = System.getProperty(PROPERTY_PULSE_CONTEXT, null);
        if (path != null)
        {
            // ensure that the path starts with a '/' but does not end with a '/'
            if (!path.startsWith("/"))
            {
                path = "/" + path;
            }
            if (path.endsWith("/"))
            {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    public static String getPulseUrl(int port)
    {
        String contextPath = getContextPath();
        if (contextPath == null)
        {
            contextPath = "";
        }

        return "http://" + MASTER_HOST + ":" + port + contextPath;
    }

    public static File getWorkingDirectory()
    {
        // from IDEA, the working directory is located in the same directory as where the projects are run.
        File workingDir = new File("./working");
        if (isAntBuild())
        {
            // from the acceptance test suite, the work.dir system property is specified
            workingDir = new File(System.getProperty(PROPERTY_WORK_DIR));
        }
        return workingDir;
    }

    private static boolean isAntBuild()
    {
        return System.getProperties().containsKey(PROPERTY_WORK_DIR);
    }

    public static File getUserHome()
    {
        return new File(getWorkingDirectory(), EnvConfig.USER_HOME);
    }

    public static File getDataDirectory() throws IOException
    {
        File userHome = getUserHome();
        Config config = loadConfigFromHome(userHome);
        if (config != null)
        {
            return new File(config.getProperty(SystemConfiguration.PULSE_DATA));
        }

        // Guess at the ./data directory in the current working directory.
        File data = new File("./data");
        if (data.exists())
        {
            return data;
        }

        // chances are that if we pick up the systems user.home we may or may not
        // be picking up the right config so we try it last.
        userHome = new File(System.getProperty(EnvConfig.USER_HOME));
        config = loadConfigFromHome(userHome);
        if (config != null)
        {
            return new File(config.getProperty(SystemConfiguration.PULSE_DATA));
        }

        return null;
    }

    /**
     * Returns the admin token for the testing agent.
     * 
     * @return the admin token for the testing agent
     * @throws IOException on error reading the token file
     */
    public static String getAgentAdminToken() throws IOException
    {
        File configDir;
        if (isAntBuild())
        {
            File agentWork = new File(getWorkingDirectory(), StartPulseTestSetup.WORK_DIR_AGENT);
            File versionHome = new File(FileSystemUtils.findFirstChildMatching(agentWork, "pulse-agent-.*"), "versions");
            configDir = new File(FileSystemUtils.findFirstChildMatching(versionHome, "[0-9]+"), "system/config");
        }
        else
        {
            configDir = new File("com.zutubi.pulse.slave/etc");
        }
        
        File tokenFile = new File(configDir, "admin.token");
        return IOUtils.fileToString(tokenFile);
    }

    /**
     * Load the config.properties instance from the specified user home directory.
     *
     * @param userHome  the user home directory being used by Pulse.
     * @return  the config instance of null if the config.properties file was not located.
     */
    private static Config loadConfigFromHome(File userHome)
    {
        File configFile = new File(userHome, MasterConfigurationManager.CONFIG_DIR + "/config.properties");
        if (configFile.exists())
        {
            return new ReadOnlyConfig(new FileConfig(configFile));
        }
        return null;
    }

    /**
     * Returns the location of a Pulse package, based on the pulse.package
     * system property.
     *
     * @return file reference to the pulse package
     * @throws IllegalStateException if pulse.package os not set or does not
     *                               refer to a valid file
     */
    public static File getPulsePackage()
    {
        return getPackage(PROPERTY_PULSE_PACKAGE);
    }

    /**
     * Returns the location of the Pulse agent package, based on the agent.package
     * system property.
     *
     * @return file reference to the pulse agent package.
     */
    public static File getAgentPackage()
    {
        return getPackage(PROPERTY_AGENT_PACKAGE);
    }

    /**
     * Returns the location of the Pulse dev package, based on the dev.package
     * system property.
     *
     * @return file reference to the pulse agent package.
     */
    public static File getDevPackage()
    {
        return getPackage(PROPERTY_DEV_PACKAGE);
    }
    
    private static File getPackage(String packageProperty)
    {
        String pkgProperty = System.getProperty(packageProperty);
        if (!StringUtils.stringSet(pkgProperty))
        {
            throw new IllegalStateException("No package specified (use the system property " + packageProperty + ")");
        }
        File pkg = new File(pkgProperty);
        if (!pkg.exists())
        {
            throw new IllegalStateException("Unexpected invalid " + packageProperty + ": " + pkg + " does not exist.");
        }
        return pkg;
    }

    /**
     * Reads the text content available at the given Pulse URI and returns it
     * as a string.  Supplies administrator credentials to log in to Pulse.
     *
     * @param contentUri uri to download the content from
     * @return the content available at the given URI, as a string
     * @throws IOException on error
     */
    public static String readUriContent(String contentUri) throws IOException
    {
        return readUriContent(contentUri, ADMIN_CREDENTIALS);
    }

    /**
     * Reads the text content available at the given Pulse URI and returns it
     * as a string.  Supplies the given credentials to log in to Pulse.
     *
     * @param contentUri  uri to download the content from
     * @param credentials credentials of a Pulse user to log in as
     * @return the content available at the given URI, as a string
     * @throws IOException on error
     */
    public static String readUriContent(String contentUri, Credentials credentials) throws IOException
    {
        InputStream input = null;
        GetMethod get = null;
        try
        {
            get = httpGet(contentUri, credentials);
            input = get.getResponseBodyAsStream();
            return IOUtils.inputStreamToString(input);
        }
        finally
        {
            IOUtils.close(input);
            releaseConnection(get);
        }
    }

    /**
     * Reads and returns an HTTP header from the given Pulse URI, returning it
     * for further inspection.  Supplies administrator credentials to log in to
     * Pulse.
     *
     * @param uri        uri to read the header from
     * @param headerName name of the header to retrieve
     * @return the found header, or null if there was no such header
     * @throws IOException on error
     */
    public static Header readHttpHeader(String uri, String headerName) throws IOException
    {
        return readHttpHeader(uri, headerName, ADMIN_CREDENTIALS);
    }

    /**
     * Reads and returns an HTTP header from the given Pulse URI, returning it
     * for further inspection.  Supplies the given credentials to log in to
     * Pulse.
     *
     * @param uri         uri to read the header from
     * @param headerName  name of the header to retrieve
     * @param credentials credentials of a Pulse user to log in as
     * @return the found header, or null if there was no such header
     * @throws IOException on error
     */
    public static Header readHttpHeader(String uri, final String headerName, Credentials credentials) throws IOException
    {
        GetMethod get = null;
        try
        {
            get = AcceptanceTestUtils.httpGet(uri, credentials);
            Header[] headers = get.getResponseHeaders();

            return CollectionUtils.find(headers, new Predicate<Header>()
            {
                public boolean satisfied(Header header)
                {
                    return header.getName().equals(headerName);
                }
            });
        }
        finally
        {
            releaseConnection(get);
        }
    }

    /**
     * Executes an HTTP get of the given Pulse URI and returns the {@link org.apache.commons.httpclient.methods.GetMethod}
     * instance for further processing.  The caller is responsible for
     * releasing the connection (by calling {@link org.apache.commons.httpclient.methods.GetMethod#releaseConnection()})
     * when it is no longer required.  Supplies the given credentials to log in
     * to Pulse.
     *
     * @param uri         uri to GET
     * @param credentials credentials of a Pulse user to log in as, or null if
     *                    no credentials should be specified
     * @return the {@link org.apache.commons.httpclient.methods.GetMethod}
     *         instance used to access the URI
     * @throws IOException on error
     */
    public static GetMethod httpGet(String uri, Credentials credentials) throws IOException
    {
        HttpClient client = new HttpClient();

        if (credentials != null)
        {
            client.getState().setCredentials(AuthScope.ANY, credentials);
            client.getParams().setAuthenticationPreemptive(true); // our Basic authentication does not challenge.
        }

        GetMethod get = new GetMethod(uri);
        int status = client.executeMethod(get);
        if (status != HttpStatus.SC_OK)
        {
            throw new RuntimeException("Get request returned status '" + status + "'");
        }

        return get;
    }

    /**
     * Safely call the {@link org.apache.commons.httpclient.HttpMethod#releaseConnection()} method.
     *
     * @param method    the method on which release connection will be called.
     */
    public static void releaseConnection(HttpMethod method)
    {
        if (method != null)
        {
            method.releaseConnection();
        }
    }

    /**
     * Dump some http method debugging information to std out.
     *
     * @param method the method being debugged.
     * @throws IOException is thrown if there are problems reading
     * the method response.
     */
    public static void debug(HttpMethod method) throws IOException
    {
        System.out.println("Status Code: " + method.getStatusCode());

        //write out the request headers
        System.out.println("*** Request ***");
        System.out.println("Request Path: " + method.getPath());
        System.out.println("Request Query: " + method.getQueryString());
        Header[] requestHeaders = method.getRequestHeaders();
        for (Header requestHeader : requestHeaders)
        {
            System.out.print(requestHeader);
        }

        //write out the response headers
        System.out.println("*** Response ***");
        System.out.println("Status Line: " + method.getStatusLine());
        Header[] responseHeaders = method.getResponseHeaders();
        for (Header responseHeader : responseHeaders)
        {
            System.out.print(responseHeader);
        }

        //write out the response body
        System.out.println("*** Response Body ***");
        System.out.println(new String(method.getResponseBody()));
    }

    /**
     * Creates a test plugin jar with the given id and name in the given
     * directory.
     * 
     * @param tmpDir temporary directory within which to create the plugin jar
     * @param id     id of the plugin to create (should be unique to the test)
     * @param name   name of the plugin to create (should be unique to the test)
     * @return the location of the plugin jar file
     * @throws IOException on error reading from the prototype plugin or
     *                     writing to the new plugin
     */
    public static File createTestPlugin(File tmpDir, String id, String name) throws IOException
    {
        File testPlugin = new File(TestUtils.getPulseRoot(), FileSystemUtils.composeFilename("com.zutubi.pulse.acceptance", "src", "test", "misc", PLUGIN_ID_TEST + ".jar"));
        File unzipDir = new File(tmpDir, "unzip");
        PulseZipUtils.extractZip(testPlugin, unzipDir);

        rewritePluginManifest(id, name, unzipDir);
        rewritePluginXml(id, unzipDir);
        rewritePluginSymbolicName(id, unzipDir);

        File pluginJarFile = new File(tmpDir, id + ".jar");
        PulseZipUtils.createZip(pluginJarFile, unzipDir, null);
        return pluginJarFile;
    }

    private static Set<String> setFromEnvironmentProperty(String property)
    {
        Set<String> set = null;
        String value = System.getenv(property);
        if (StringUtils.stringSet(value))
        {
            set = new HashSet<String>(asList(value.split(",")));
        }
        return set;
    }

    public static boolean includeTestClass(Class<? extends TestCase> testClass)
    {
        Set<String> included = setFromEnvironmentProperty("PULSE_INCLUDED_TESTS");
        Set<String> excluded = setFromEnvironmentProperty("PULSE_EXCLUDED_TESTS");

        String name = testClass.getSimpleName();
        return (included == null || included.contains(name)) && (excluded == null || !excluded.contains(name));
    }

    public static void addClassToSuite(TestSuite targetSuite, Class<? extends TestCase> testClass)
    {
        if (includeTestClass(testClass))
        {
            targetSuite.addTestSuite(testClass);
        }
    }

    private static void rewritePluginManifest(String id, String name, File unzipDir) throws IOException
    {
        File manifestFile = new File(unzipDir, FileSystemUtils.composeFilename("META-INF", "MANIFEST.MF"));
        String manifest = IOUtils.fileToString(manifestFile);
        manifest = manifest.replaceAll(PLUGIN_ID_TEST, id);
        manifest = manifest.replaceAll("Test Post-Processor", name);
        FileSystemUtils.createFile(manifestFile, manifest);
    }

    private static void rewritePluginXml(String id, File unzipDir) throws IOException
    {
        File pluginXmlFile = new File(unzipDir, "plugin.xml");
        String xml = IOUtils.fileToString(pluginXmlFile);
        xml = xml.replaceAll("test\\.pp", id + ".pp");
        FileSystemUtils.createFile(pluginXmlFile, xml);
    }

    private static void rewritePluginSymbolicName(String id, File unzipDir) throws IOException
    {
        File configClassFile = new File(unzipDir, FileSystemUtils.composeFilename("com", "zutubi", "pulse", "core", "postprocessors", "test", "TestPostProcessorConfiguration.class"));
        JavaClass javaClass = parseClass(configClassFile);
        ConstantPool constantPool = javaClass.getConstantPool();
        for (int i = 0; i < constantPool.getLength(); i++)
        {
            Constant constant = constantPool.getConstant(i);
            if (constant instanceof ConstantUtf8)
            {
                ConstantUtf8 utf8 = (ConstantUtf8) constant;
                String value = utf8.getBytes();
                if (value.equals("zutubi.testPostProcessorConfig"))
                {
                    utf8.setBytes(id);
                }
            }
        }
        javaClass.dump(configClassFile);
    }

    private static JavaClass parseClass(File configClassFile) throws IOException
    {
        FileInputStream is = null;
        try
        {
            is = new FileInputStream(configClassFile);
            ClassParser classParser = new ClassParser(is, configClassFile.getName());
            return classParser.parse();
        }
        finally
        {
            IOUtils.close(is);
        }
    }
}
