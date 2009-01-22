package com.zutubi.pulse.servercore.jetty;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
//import org.apache.ivy.plugins.repository.url.URLRepository;
import org.mortbay.jetty.Server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

// this is somewhere in between a unit test and an acceptance test.  It is not a unit test
// because it does more than just deal with the ArtifactRepositoryConfigurationHandler, but
// it is less than an acceptance test because it does not use the repository that is running
// within the deployed pulse installation.  So where does this test fit?.
public class ArtifactRepositoryTest extends PulseTestCase
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
        repository.setHost("localhost");
        repository.setPort(8888);
        repository.setBase(repositoryBase);

        baseRepoUrl = "http://localhost:8888/";

        startServer(serverManager.createNewServer("repository", repository));
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        serverManager.stop(true);

        super.tearDown();
    }

    public void testPublishAndRetrieve() throws IOException, InterruptedException
    {
        assertCanRetrieve("sample/a.txt", "sample content");
        assertCanRetrieve("a.txt", "a quick brown fox");

        assertCanPublish("sample/b.txt", "sample content");
        assertCanPublish("b.txt", "jumped over the log");
    }

/*
    public void testDelete() throws IOException
    {
        File repoFile = createFile(repositoryBase, "x.txt", "sample");
        assertTrue(repoFile.isFile());

        URLRepository repositoryClient = new URLRepository();
        repositoryClient.put(null, baseRepoUrl + "x.txt", true);

        assertFalse(repoFile.isFile());
    }
*/

    public void testBrowse() throws IOException
    {
        // this needs selenium, so is more in line with the existing acceptance tests.
        // create a bunch of files, and verify that we can browse through the UI.
        createFile(repositoryBase, "x.txt", "sample");
        createFile(repositoryBase, "a/a.txt", "sample");
        createFile(repositoryBase, "a/b.txt", "sample");
        createFile(repositoryBase, "a/b/1.txt", "sample");
        createFile(repositoryBase, "a/b/2.txt", "sample");

/*
        URLRepository repositoryClient = new URLRepository();
        List listing = repositoryClient.list(baseRepoUrl);
*/

        // bug in the html returned by the Resource.getListHtml from jetty prevents the correct interpretation of the response.
//        assertEquals(2, listing.size());

        // navigate to http://localhost:8888
        // assert link present for x.txt
        // assert link present for a
        // click link a
        // assert link present for a.txt
        // assert link present for b.txt
        // assert link present for b
        // assert link present for parent directory.
        // click link b
        // assert link present for 1.txt
        // assert link present for 2.txt
        // assert link present for parent directory.
    }

    private void assertCanPublish(String path, String content) throws IOException
    {
/*
        File toPublish = createFile(tmp, "localfile.txt", content);

        // use ivy to handle the interaction with the repository.
        URLRepository repositoryClient = new URLRepository();
        repositoryClient.put(toPublish, baseRepoUrl + path, true);

        File uploadedRepositoryFile = new File(repositoryBase, path);
        assertTrue(uploadedRepositoryFile.isFile());
        assertEquals(content, IOUtils.fileToString(uploadedRepositoryFile));
*/
    }

    private void assertCanRetrieve(String path, String content) throws IOException
    {
/*
        createFile(repositoryBase, path, content);

        File dest = new File(tmp, "localfile.txt");

        // use ivy to handle the interaction with the repository.
        URLRepository repositoryClient = new URLRepository();
        repositoryClient.get(baseRepoUrl + path, dest);

        assertEquals(content, IOUtils.fileToString(dest));
*/
    }

    private void startServer(final Server server) throws InterruptedException
    {
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {

                try
                {
                    server.start();
                    while (server.isStarted())
                    {
                        Thread.sleep(1000);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        Thread.sleep(1000);
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
        IOUtils.writeToFile(newFile, new ByteArrayInputStream(content.getBytes()));

        return newFile;
    }
}
