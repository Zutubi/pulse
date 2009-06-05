package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.jetty.SecurityHandler;
import com.zutubi.pulse.servercore.jetty.ArtifactRepositoryConfigurationHandler;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.pulse.servercore.jetty.ServerConfigurationHandler;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import org.apache.ivy.plugins.repository.url.URLRepository;
import static org.mockito.Mockito.*;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;

import java.io.File;
import java.io.IOException;
import java.util.List;

// this is somewhere in between a unit test and an acceptance test.  It is not a unit test
// because it does more than just deal with the ArtifactRepositoryConfigurationHandler, but
// it is less than an acceptance test because it does not use the repository that is running
// within the deployed pulse installation.  
public class AritfactRepositoryIsolationTest extends PulseTestCase
{
    private File tmp;
    private File repositoryBase;
    private String baseRepoUrl;

    private JettyServerManager serverManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();
        repositoryBase = new File(tmp, "repositoryBase");

        serverManager = new JettyServerManager();

        ArtifactRepositoryConfigurationHandler repository = new ArtifactRepositoryConfigurationHandler();
        repository.setBase(repositoryBase);

        // disable security until we can sort out how to properly set the credentials.
        AccessManager accessManager = mock(AccessManager.class);
        stub(accessManager.hasPermission(anyString(), anyObject())).toReturn(true);
        SecurityHandler security = new SecurityHandler();
        security.setAccessManager(accessManager);
        repository.setSecurityHandler(security);

        final int port = 8765;
        final String host = "localhost";

        baseRepoUrl = "http://"+host+":"+port+"/repository";

        Server server = serverManager.configureServer("test", new ServerConfigurationHandler()
        {
            public void configure(Server server) throws IOException
            {
                SocketListener listener = new SocketListener();
                listener.setHost(host);
                listener.setPort(port);
                server.addListener(listener);
            }
        });

        serverManager.configureContext("test", "/repository", repository);

        server.start();
    }

    protected void tearDown() throws Exception
    {
        serverManager.stop(true);
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testPublishAndRetrieve() throws IOException, InterruptedException
    {
        assertCanRetrieve("sample/a.txt", "sample content");
        assertCanRetrieve("a.txt", "a quick brown fox");

        assertCanPublish("zutubi/com.zutubi.sample/txts/b.txt", "sample content");
        assertCanPublish("b.txt", "jumped over the log");
    }

    public void testBrowse() throws IOException
    {
        createFile(repositoryBase, "x.txt", "sample");
        createFile(repositoryBase, "a/a.txt", "sample");
        createFile(repositoryBase, "a/b.txt", "sample");
        createFile(repositoryBase, "a/b/1.txt", "sample");
        createFile(repositoryBase, "a/b/2.txt", "sample");

        assertListing(baseRepoUrl, "x.txt");
        assertListing(baseRepoUrl + "/a", "a.txt", "b.txt");
        assertListing(baseRepoUrl + "/a/b", "1.txt", "2.txt");
    }

    private void assertListing(String url, String... expected) throws IOException
    {
        URLRepository repositoryClient = new URLRepository();
        List listing = repositoryClient.list(url);

        // for some reason the URLRepository listing does not return
        // directories, annoying but not fatal.

        assertEquals(expected.length, listing.size());
        for (String s : expected)
        {
            assertTrue(listing.contains(url + "/" + s));
        }
    }

    private void assertCanPublish(String path, String content) throws IOException
    {
        File toPublish = createFile(tmp, "localfile.txt", content);

        // use ivy to handle the interaction with the repository.
        URLRepository repositoryClient = new URLRepository();
        repositoryClient.put(toPublish, baseRepoUrl + "/" + path, true);

        File uploadedRepositoryFile = new File(repositoryBase, path);
        assertTrue(uploadedRepositoryFile.isFile());
        assertEquals(content, IOUtils.fileToString(uploadedRepositoryFile));
    }

    private void assertCanRetrieve(String path, String content) throws IOException
    {
        createFile(repositoryBase, path, content);

        File dest = new File(tmp, "localfile.txt");

        // use ivy to handle the interaction with the repository.
        URLRepository repositoryClient = new URLRepository();
        repositoryClient.get(baseRepoUrl + "/" + path, dest);

        assertEquals(content, IOUtils.fileToString(dest));
    }

    private File createFile(File base, String path, String content) throws IOException
    {
        File newFile = new File(base, path);
        File dir = newFile.getParentFile();
        if (!dir.isDirectory())
        {
            assertTrue(dir.mkdirs());
        }
        if (!newFile.isFile())
        {
            assertTrue(newFile.createNewFile());
        }
        FileSystemUtils.createFile(newFile, content);

        return newFile;
    }
}
