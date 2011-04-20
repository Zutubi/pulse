package com.zutubi.pulse.core.scm.hg;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.hg.config.MercurialConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.io.IOUtils;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class MercurialClientTest extends PulseTestCase
{
    private static final String REVISION_DEFAULT_LATEST = "fe4571fd8bad5d556b26d1a05806074e67bbfa97";
    private static final String REVISION_DEFAULT_PREVIOUS = "60d7c7d4e20cbb29e5bf9b56651fd9cd0255e3be";
    private static final String REVISION_DEFAULT_TWO_PREVIOUS = "0040f780ba9a5905d059d26d777bb6cd78cdb96f";
    private static final String REVISION_BRANCH_LATEST = "04010fd8851efebac7f36ad39d246a4970806109";
    private static final String REVISION_BRANCH_PREVIOUS = "867f406a6a399c66e7f6a16e3a0a292b03484404";
    private static final String REVISION_BRANCH_TWO_PREVIOUS = "805df7c8e75b6d9e2739a74fb5c69f35bdf9be3d";
    private static final String REVISION_MULTILINE = "7e57b6bda144ef2688363ad58d250460cf6a422c";
    private static final String REVISION_MULTILINE_PREVIOUS = "5ee4cf0fdf08fedc05a483253b087243481728dd";

    private static final String BRANCH ="1.0";

    private static final String CONTENT_FILE_PATH = "exercise1/hello.c";
    private static final String CONTENT_DEFAULT_LATEST = "#include <stdio.h>\n" +
            "\n" +
            "int main(int argc, char **argv)\n" +
            "{\n" +
            "    int i;\n" +
            "    for (i = 1; i < argc; i++)\n" +
            "    {\n" +
            "        printf(\"Why hello there, %s!\\n\", argv[i]);\n" +
            "    }\n" +
            "\n" +
            "    return 0;\n" +
            "}\n";

    private static final String CONTENT_DEFAULT_PREVIOUS = "#include <stdio.h>\n" +
            "\n" +
            "int main(int argc, char **argv)\n" +
            "{\n" +
            "    if (argc < 2)\n" +
            "    {\n" +
            "        fprintf(stderr, \"Usage: %s <name>\\n\", argv[0]);\n" +
            "        return 1;\n" +
            "    }\n" +
            "\n" +
            "    printf(\"Why hello there, %s!\\n\", argv[1]);\n" +
            "    return 0;\n" +
            "}\n";

    private static final String CONTENT_BRANCH_LATEST = "#include <stdio.h>\n" +
            "\n" +
            "int main(int argc, char **argv)\n" +
            "{\n" +
            "    if (argc < 2)\n" +
            "    {\n" +
            "        fprintf(stderr, \"Usage: %s <name>\\n\", argv[0]);\n" +
            "        return 1;\n" +
            "    }\n" +
            "\n" +
            "    printf(\"Hello, %s!\\n\", argv[1]);\n" +
            "    return 0;\n" +
            "}\n";

    private static final String CONTENT_BRANCH_PREVIOUS = "#include <stdio.h>\n" +
            "\n" +
            "int main(int argc, char *argv[])\n" +
            "{\n" +
            "    if (argc < 2)\n" +
            "    {\n" +
            "        fprintf(stderr, \"Usage: %s <name>\\n\", argv[0]);\n" +
            "        return 1;\n" +
            "    }\n" +
            "\n" +
            "    printf(\"Hello, %s!\\n\", argv[1]);\n" +
            "    return 0;\n" +
            "}\n";

    private static final String OUTPUT_NO_UPDATES = "0 files updated, 0 files merged, 0 files removed, 0 files unresolved";
    private static final String OUTPUT_ONE_UPDATE = "1 files updated, 0 files merged, 0 files removed, 0 files unresolved";

    private File tmp;
    private File repositoryBase;
    private File workingDir;
    private String repository;
    private MercurialClient client;
    private MercurialConfiguration config;
    private PulseExecutionContext context;
    private RecordingScmFeedbackHandler handler;
    private ScmContextImpl scmContext;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();

        URL url = getClass().getResource("MercurialClientTest.repo.zip");
        PulseZipUtils.extractZip(new File(url.toURI()), new File(tmp, "repo"));

        repositoryBase = new File(tmp, "repo");
        repository = repositoryBase.getCanonicalPath();

        config = new MercurialConfiguration();
        config.setRepository(repository);
        client = new MercurialClient(config);

        workingDir = new File(tmp, "wd");
        context = new PulseExecutionContext();
        context.setWorkingDir(workingDir);

        File persistentWorkingDir = new File(tmp, "scm");
        assertTrue(persistentWorkingDir.mkdir());
        scmContext = new ScmContextImpl();
        scmContext.setPersistentWorkingDir(persistentWorkingDir);

        handler = new RecordingScmFeedbackHandler();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testGetUid() throws ScmException
    {
        assertEquals(repository, client.getUid());
    }

    public void testGetLocation() throws ScmException
    {
        assertEquals(repository, client.getLocation());
    }

    public void testClose()
    {
        client.close();
    }

    public void testDestroy() throws ScmException
    {
        ScmFeedbackAdapter handler = new ScmFeedbackAdapter();
        client.init(scmContext, handler);
        client.destroy(scmContext, handler);
    }

    public void testStoreConnectionDetails() throws IOException, ScmException
    {
        client.storeConnectionDetails(new PulseExecutionContext(), workingDir);
    }

    public void testGetEOLStyle() throws ScmException
    {
        assertEquals(EOLStyle.BINARY, client.getEOLPolicy(context));
    }

    public void testGetProperties() throws ScmException
    {
        List<ResourceProperty> properties = client.getProperties(new PulseExecutionContext());
        assertEquals(1, properties.size());
        ResourceProperty repositoryProperty = properties.get(0);
        assertEquals(repository, repositoryProperty.getValue());
    }

    public void testCheckout() throws ScmException, ParseException, IOException
    {
        Revision rev = client.checkout(context, null, handler);

        assertEquals(REVISION_DEFAULT_LATEST, rev.getRevisionString());
        assertLatestCheckedOut();
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testCheckoutRevision() throws ScmException, ParseException, IOException
    {
        Revision rev = client.checkout(context, new Revision(REVISION_DEFAULT_PREVIOUS), handler);

        assertEquals(REVISION_DEFAULT_PREVIOUS, rev.getRevisionString());
        assertFileContent(CONTENT_DEFAULT_PREVIOUS, CONTENT_FILE_PATH);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testCheckoutOnBranch() throws ScmException, ParseException, IOException
    {
        config.setBranch(BRANCH);
        Revision rev = client.checkout(context, null, handler);

        assertEquals(REVISION_BRANCH_LATEST, rev.getRevisionString());
        assertFileContent(CONTENT_BRANCH_LATEST, CONTENT_FILE_PATH);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testCheckoutRevisionOnBranch() throws ScmException, ParseException, IOException
    {
        config.setBranch(BRANCH);
        Revision rev = client.checkout(context, new Revision(REVISION_BRANCH_PREVIOUS), handler);

        assertEquals(REVISION_BRANCH_PREVIOUS, rev.getRevisionString());
        assertFileContent(CONTENT_BRANCH_PREVIOUS, CONTENT_FILE_PATH);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testGetLatestRevision() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        Revision rev = client.getLatestRevision(scmContext);

        assertEquals(REVISION_DEFAULT_LATEST, rev.getRevisionString());
    }

    public void testGetLatestRevisionOnBranch() throws ScmException
    {
        config.setBranch(BRANCH);
        client.init(scmContext, new ScmFeedbackAdapter());
        Revision rev = client.getLatestRevision(scmContext);

        assertEquals(REVISION_BRANCH_LATEST, rev.getRevisionString());
    }

    public void testGetRevisions() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        List<Revision> revisions = client.getRevisions(scmContext, new Revision(REVISION_DEFAULT_TWO_PREVIOUS), null);
        assertEquals(asList(new Revision(REVISION_DEFAULT_PREVIOUS), new Revision(REVISION_DEFAULT_LATEST)), revisions);
    }

    public void testGetRevisionsInRange() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        List<Revision> revisions = client.getRevisions(scmContext, new Revision(REVISION_DEFAULT_TWO_PREVIOUS), new Revision(REVISION_DEFAULT_LATEST));
        assertEquals(asList(new Revision(REVISION_DEFAULT_PREVIOUS), new Revision(REVISION_DEFAULT_LATEST)), revisions);
    }

    public void testGetRevisionsInReverseRange() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        List<Revision> revisions = client.getRevisions(scmContext, new Revision(REVISION_DEFAULT_LATEST), new Revision(REVISION_DEFAULT_TWO_PREVIOUS));
        assertEquals(asList(new Revision(REVISION_DEFAULT_PREVIOUS), new Revision(REVISION_DEFAULT_TWO_PREVIOUS)), revisions);
    }

    public void testRetrieve() throws ScmException, IOException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        InputStream content = client.retrieve(scmContext, CONTENT_FILE_PATH, null);
        assertEquals(CONTENT_DEFAULT_LATEST, IOUtils.inputStreamToString(content));
    }

    public void testRetrieveAtRevision() throws ScmException, IOException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        InputStream content = client.retrieve(scmContext, CONTENT_FILE_PATH, new Revision(REVISION_DEFAULT_PREVIOUS));
        assertEquals(CONTENT_DEFAULT_PREVIOUS, IOUtils.inputStreamToString(content));
    }

    public void testRetrieveOnBranch() throws ScmException, IOException
    {
        config.setBranch(BRANCH);
        client.init(scmContext, new ScmFeedbackAdapter());
        InputStream content = client.retrieve(scmContext, CONTENT_FILE_PATH, null);
        assertEquals(CONTENT_BRANCH_LATEST, IOUtils.inputStreamToString(content));
    }

    public void testRetrieveOnBranchAtRevision() throws ScmException, IOException
    {
        config.setBranch(BRANCH);
        client.init(scmContext, new ScmFeedbackAdapter());
        InputStream content = client.retrieve(scmContext, CONTENT_FILE_PATH, new Revision(REVISION_BRANCH_PREVIOUS));
        assertEquals(CONTENT_BRANCH_PREVIOUS, IOUtils.inputStreamToString(content));
    }

    public void testUpdateNoNewRevisions() throws ScmException
    {
        client.checkout(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));

        handler.reset();

        Revision rev = client.update(context, null, handler);
        assertEquals(REVISION_DEFAULT_LATEST, rev.getRevisionString());
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals(OUTPUT_NO_UPDATES, handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
    }

    public void testUpdateNewRevision() throws ScmException, IOException
    {
        client.checkout(context, new Revision(REVISION_DEFAULT_PREVIOUS), handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));

        handler.reset();

        Revision rev = client.update(context, null, handler);
        assertEquals(REVISION_DEFAULT_LATEST, rev.getRevisionString());
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals(OUTPUT_ONE_UPDATE, handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
        assertFileContent(CONTENT_DEFAULT_LATEST, CONTENT_FILE_PATH);
    }

    public void testUpdateToRevision() throws ScmException, IOException
    {
        client.checkout(context, new Revision(REVISION_DEFAULT_TWO_PREVIOUS), handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));

        handler.reset();

        Revision rev = client.update(context, new Revision(REVISION_DEFAULT_PREVIOUS), handler);
        assertEquals(REVISION_DEFAULT_PREVIOUS, rev.getRevisionString());
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals(OUTPUT_ONE_UPDATE, handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
        assertFileContent(CONTENT_DEFAULT_PREVIOUS, CONTENT_FILE_PATH);
    }

    public void testUpdateOnBranchNoNewRevisions() throws ScmException
    {
        config.setBranch(BRANCH);
        client.checkout(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));

        handler.reset();

        Revision rev = client.update(context, null, handler);
        assertEquals(REVISION_BRANCH_LATEST, rev.getRevisionString());
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals(OUTPUT_NO_UPDATES, handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
    }

    public void testUpdateOnBranchNewRevision() throws ScmException, IOException
    {
        config.setBranch(BRANCH);
        client.checkout(context, new Revision(REVISION_BRANCH_PREVIOUS), handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));

        handler.reset();

        Revision rev = client.update(context, null, handler);
        assertEquals(REVISION_BRANCH_LATEST, rev.getRevisionString());
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals(OUTPUT_ONE_UPDATE, handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
        assertFileContent(CONTENT_BRANCH_LATEST, CONTENT_FILE_PATH);
    }

    public void testUpdateOnBranchToRevision() throws ScmException, IOException
    {
        config.setBranch(BRANCH);
        client.checkout(context, new Revision(REVISION_BRANCH_TWO_PREVIOUS), handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));

        handler.reset();

        Revision rev = client.update(context, new Revision(REVISION_BRANCH_PREVIOUS), handler);
        assertEquals(REVISION_BRANCH_PREVIOUS, rev.getRevisionString());
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals(OUTPUT_ONE_UPDATE, handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
        assertFileContent(CONTENT_BRANCH_PREVIOUS, CONTENT_FILE_PATH);
    }

    public void testUpdateNoCheckout() throws ScmException, IOException
    {
        client.update(context, null, handler);
        assertLatestCheckedOut();
    }

    public void testGetChanges() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        List<Changelist> changes = client.getChanges(scmContext, new Revision(REVISION_DEFAULT_TWO_PREVIOUS), null);
        assertEquals(2, changes.size());
        assertEquals(CONTENT_FILE_PATH, changes.get(0).getChanges().get(0).getPath());
        assertEquals(CONTENT_FILE_PATH, changes.get(1).getChanges().get(0).getPath());
    }

    public void testGetChangesFullHistory() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        List<Changelist> changes = client.getChanges(scmContext, new Revision(MercurialConstants.REVISION_ZERO), null);
        assertEquals(10, changes.size());
    }

    public void testGetChangesOnBranch() throws ScmException
    {
        config.setBranch(BRANCH);
        client.init(scmContext, new ScmFeedbackAdapter());
        List<Changelist> changes = client.getChanges(scmContext, new Revision(REVISION_BRANCH_TWO_PREVIOUS), null);
        assertEquals(2, changes.size());
        assertEquals("Fixed bug: errors should go to stderr.", changes.get(0).getComment());
    }

    public void testLatestChangesWithExcludes() throws ScmException
    {
        config.setFilterPaths(asList("**/*"));
        client.init(scmContext, new ScmFeedbackAdapter());

        List<Changelist> changes = client.getChanges(scmContext, new Revision(MercurialConstants.REVISION_ZERO), null);
        assertEquals(0, changes.size());
    }

    public void testLatestChangesWithSpecificExcludes() throws ScmException
    {
        config.setFilterPaths(asList(CONTENT_FILE_PATH));
        client.init(scmContext, new ScmFeedbackAdapter());

        List<Changelist> changes = client.getChanges(scmContext, new Revision(MercurialConstants.REVISION_ZERO), null);
        assertEquals(5, changes.size());
        Changelist changelist = changes.get(0);
        List<FileChange> fileChanges = changelist.getChanges();
        assertEquals(asList("empty.txt", CONTENT_FILE_PATH, "file1.txt", "file2.txt"), CollectionUtils.map(fileChanges, new Mapping<FileChange, String>()
        {
            public String map(FileChange fileChange)
            {
                return fileChange.getPath();
            }
        }));
    }

    public void testChangeWithMultilineComment() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());

        List<Changelist> changes = client.getChanges(scmContext, new Revision(REVISION_MULTILINE_PREVIOUS), new Revision(REVISION_MULTILINE));
        assertEquals(1, changes.size());
        assertEquals("Update my exercise to be more interactive.\n" +
                "\n" +
                "Note that at this stage we don't check the number of arguments.", changes.get(0).getComment());
    }

    public void testTestConnectionOK() throws ScmException
    {
        client.testConnection();
    }

    public void testTestConnectionBadRepo()
    {
        config.setRepository("file:///no/such/repo");
        try
        {
            client.testConnection();
            fail("Test of bad repo should fail");
        }
        catch (ScmException e)
        {
            assertThat(e.getMessage(), containsString("not found"));
        }
    }

    public void testTestConnectionBadBranch()
    {
        config.setBranch("nosuchbranch");
        try
        {
            client.testConnection();
            fail("Test of bad branch should fail");
        }
        catch (ScmException e)
        {
            assertThat(e.getMessage(), containsString("unknown branch"));
        }
    }

    public void testTagRevision() throws ScmException, IOException
    {
        final String TAG_NAME = "test-tag";

        client.init(scmContext, new ScmFeedbackAdapter());
        client.tag(scmContext, context, new Revision(REVISION_DEFAULT_PREVIOUS), TAG_NAME, false);

        assertTag(TAG_NAME, REVISION_DEFAULT_PREVIOUS);
    }

    public void testMoveExistingTag() throws ScmException, IOException
    {
        final String TAG_NAME = "test-tag";

        client.init(scmContext, new ScmFeedbackAdapter());
        client.tag(scmContext, context, new Revision(REVISION_DEFAULT_PREVIOUS), TAG_NAME, false);
        client.tag(scmContext, context, new Revision(REVISION_DEFAULT_LATEST), TAG_NAME, true);

        assertTag(TAG_NAME, REVISION_DEFAULT_LATEST);
    }

    private void assertTag(String tagName, String expectedRevision) throws ScmException
    {
        MercurialCore hg = new MercurialCore();
        hg.setWorkingDirectory(repositoryBase);
        Map<String,String> tags = hg.tags();
        assertTrue(tags.containsKey(tagName));
        assertTrue(expectedRevision.startsWith(tags.get(tagName)));
    }

    private void assertLatestCheckedOut() throws IOException
    {
        assertFileContent(CONTENT_DEFAULT_LATEST, CONTENT_FILE_PATH);
        assertMercurialDir(workingDir);
    }

    private void assertFileContent(String expected, String path) throws IOException
    {
        File file = new File(workingDir, path);
        assertTrue(file.isFile());
        String content = IOUtils.fileToString(file);
        assertEquals(normaliseLineEndings(expected), normaliseLineEndings(content));
    }

    private String normaliseLineEndings(String s)
    {
        return s.replaceAll("\\r\\n", "\n");
    }

    private void assertMercurialDir(File workingDir)
    {
        assertTrue(new File(workingDir, ".hg").isDirectory());
    }
}
