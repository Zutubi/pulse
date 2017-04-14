/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.scm.hg;

import com.google.common.base.Function;
import com.google.common.io.Files;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.hg.config.MercurialConfiguration;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;

public class MercurialClientTest extends MercurialTestBase
{
    private MercurialClient client;
    private MercurialConfiguration config;

    protected void setUp() throws Exception
    {
        super.setUp();

        config = new MercurialConfiguration();
        config.setRepository(repository);
        client = new MercurialClient(config);
    }

    public void testGetUid() throws ScmException
    {
        assertEquals(repository, client.getUid(scmContext));
    }

    public void testGetLocation() throws ScmException
    {
        assertEquals(repository, client.getLocation(scmContext));
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
        client.storeConnectionDetails(new PulseExecutionContext(), baseDir);
    }

    public void testGetEOLStyle() throws ScmException
    {
        assertEquals(EOLStyle.BINARY, client.getEOLPolicy(buildContext));
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
        Revision rev = client.checkout(buildContext, null, handler);

        assertEquals(REVISION_DEFAULT_LATEST, rev.getRevisionString());
        assertLatestCheckedOut();
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testCheckoutRevision() throws ScmException, ParseException, IOException
    {
        Revision rev = client.checkout(buildContext, new Revision(REVISION_DEFAULT_PREVIOUS), handler);

        assertEquals(REVISION_DEFAULT_PREVIOUS, rev.getRevisionString());
        assertFileContent(CONTENT_DEFAULT_PREVIOUS, CONTENT_FILE_PATH);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testCheckoutOnBranch() throws ScmException, ParseException, IOException
    {
        config.setBranch(BRANCH);
        Revision rev = client.checkout(buildContext, null, handler);

        assertEquals(REVISION_BRANCH_LATEST, rev.getRevisionString());
        assertFileContent(CONTENT_BRANCH_LATEST, CONTENT_FILE_PATH);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testCheckoutRevisionOnBranch() throws ScmException, ParseException, IOException
    {
        config.setBranch(BRANCH);
        Revision rev = client.checkout(buildContext, new Revision(REVISION_BRANCH_PREVIOUS), handler);

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
        assertEquals(CONTENT_DEFAULT_LATEST, ScmUtils.retrieveContent(client, scmContext, CONTENT_FILE_PATH, null));
    }

    public void testRetrieveAtRevision() throws ScmException, IOException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(CONTENT_DEFAULT_PREVIOUS, ScmUtils.retrieveContent(client, scmContext, CONTENT_FILE_PATH, new Revision(REVISION_DEFAULT_PREVIOUS)));
    }

    public void testRetrieveOnBranch() throws ScmException, IOException
    {
        config.setBranch(BRANCH);
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(CONTENT_BRANCH_LATEST, ScmUtils.retrieveContent(client, scmContext, CONTENT_FILE_PATH, null));
    }

    public void testRetrieveOnBranchAtRevision() throws ScmException, IOException
    {
        config.setBranch(BRANCH);
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(CONTENT_BRANCH_PREVIOUS, ScmUtils.retrieveContent(client, scmContext, CONTENT_FILE_PATH, new Revision(REVISION_BRANCH_PREVIOUS)));
    }

    public void testUpdateNoNewRevisions() throws ScmException
    {
        client.checkout(buildContext, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));

        handler.reset();

        Revision rev = client.update(buildContext, null, handler);
        assertEquals(REVISION_DEFAULT_LATEST, rev.getRevisionString());
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals(OUTPUT_NO_UPDATES, handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
    }

    public void testUpdateNewRevision() throws ScmException, IOException
    {
        client.checkout(buildContext, new Revision(REVISION_DEFAULT_PREVIOUS), handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));

        handler.reset();

        Revision rev = client.update(buildContext, null, handler);
        assertEquals(REVISION_DEFAULT_LATEST, rev.getRevisionString());
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals(OUTPUT_ONE_UPDATE, handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
        assertFileContent(CONTENT_DEFAULT_LATEST, CONTENT_FILE_PATH);
    }

    public void testUpdateToRevision() throws ScmException, IOException
    {
        client.checkout(buildContext, new Revision(REVISION_DEFAULT_TWO_PREVIOUS), handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));

        handler.reset();

        Revision rev = client.update(buildContext, new Revision(REVISION_DEFAULT_PREVIOUS), handler);
        assertEquals(REVISION_DEFAULT_PREVIOUS, rev.getRevisionString());
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals(OUTPUT_ONE_UPDATE, handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
        assertFileContent(CONTENT_DEFAULT_PREVIOUS, CONTENT_FILE_PATH);
    }

    public void testUpdateOnBranchNoNewRevisions() throws ScmException
    {
        config.setBranch(BRANCH);
        client.checkout(buildContext, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));

        handler.reset();

        Revision rev = client.update(buildContext, null, handler);
        assertEquals(REVISION_BRANCH_LATEST, rev.getRevisionString());
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals(OUTPUT_NO_UPDATES, handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
    }

    public void testUpdateOnBranchNewRevision() throws ScmException, IOException
    {
        config.setBranch(BRANCH);
        client.checkout(buildContext, new Revision(REVISION_BRANCH_PREVIOUS), handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));

        handler.reset();

        Revision rev = client.update(buildContext, null, handler);
        assertEquals(REVISION_BRANCH_LATEST, rev.getRevisionString());
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals(OUTPUT_ONE_UPDATE, handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
        assertFileContent(CONTENT_BRANCH_LATEST, CONTENT_FILE_PATH);
    }

    public void testUpdateOnBranchToRevision() throws ScmException, IOException
    {
        config.setBranch(BRANCH);
        client.checkout(buildContext, new Revision(REVISION_BRANCH_TWO_PREVIOUS), handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));

        handler.reset();

        Revision rev = client.update(buildContext, new Revision(REVISION_BRANCH_PREVIOUS), handler);
        assertEquals(REVISION_BRANCH_PREVIOUS, rev.getRevisionString());
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals(OUTPUT_ONE_UPDATE, handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
        assertFileContent(CONTENT_BRANCH_PREVIOUS, CONTENT_FILE_PATH);
    }

    public void testUpdateNoCheckout() throws ScmException, IOException
    {
        client.update(buildContext, null, handler);
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
        config.setExcludedPaths(asList("**/*"));
        client.init(scmContext, new ScmFeedbackAdapter());

        List<Changelist> changes = client.getChanges(scmContext, new Revision(MercurialConstants.REVISION_ZERO), null);
        assertEquals(0, changes.size());
    }

    public void testLatestChangesWithSpecificExcludes() throws ScmException
    {
        config.setExcludedPaths(asList(CONTENT_FILE_PATH));
        client.init(scmContext, new ScmFeedbackAdapter());

        List<Changelist> changes = client.getChanges(scmContext, new Revision(MercurialConstants.REVISION_ZERO), null);
        assertEquals(5, changes.size());
        Changelist changelist = changes.get(0);
        List<FileChange> fileChanges = changelist.getChanges();
        assertEquals(asList("empty.txt", CONTENT_FILE_PATH, "file1.txt", "file2.txt"), transform(fileChanges, new Function<FileChange, String>()
        {
            public String apply(FileChange fileChange)
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
        client.testConnection(scmContext);
    }

    public void testTestConnectionBadRepo()
    {
        config.setRepository("file:///no/such/repo");
        try
        {
            client.testConnection(scmContext);
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
            client.testConnection(scmContext);
            fail("Test of bad branch should fail");
        }
        catch (ScmException e)
        {
            assertThat(e.getMessage(), containsString("unknown branch"));
        }
    }

    public void testBrowseRoot() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        List<ScmFile> files = client.browse(scmContext, "", null);
        Collections.sort(files);
        assertEquals(asList(
                new ScmFile("bin", true),
                new ScmFile("empty.txt", false),
                new ScmFile("exercise1", true),
                new ScmFile("file1.txt", false),
                new ScmFile("file2.txt", false)
        ), files);
    }

    public void testBrowseRootAtRevision() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        List<ScmFile> files = client.browse(scmContext, "", new Revision("1"));
        Collections.sort(files);
        assertEquals(asList(
                new ScmFile("empty.txt", false),
                new ScmFile("exercise1", true),
                new ScmFile("file1.txt", false),
                new ScmFile("file2.txt", false)
        ), files);
    }

    public void testBrowseSubdirectoryAtRevision() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        List<ScmFile> files = client.browse(scmContext, "exercise1", null);
        assertEquals(asList(new ScmFile("exercise1/hello.c", false)), files);
    }

    public void testBrowseNonExistant() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        try
        {
            client.browse(scmContext, "nosuchdir", null);
            fail("Should not be able to browse bad directory");
        }
        catch (ScmException e)
        {
            assertThat(e.getMessage(), containsString("Cannot list contents of path"));
        }
    }
    
    public void testBrowseFile() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        try
        {
            client.browse(scmContext, "file1.txt", null);
            fail("Should not be able to browse a file");
        }
        catch (ScmException e)
        {
            assertThat(e.getMessage(), containsString("Cannot list contents of path"));
        }
    }
    
    public void testTagRevision() throws ScmException, IOException
    {
        final String TAG_NAME = "test-tag";

        client.init(scmContext, new ScmFeedbackAdapter());
        client.tag(scmContext, new Revision(REVISION_DEFAULT_PREVIOUS), TAG_NAME, false);

        assertTag(TAG_NAME, REVISION_DEFAULT_PREVIOUS);
    }

    public void testMoveExistingTag() throws ScmException, IOException
    {
        final String TAG_NAME = "test-tag";

        client.init(scmContext, new ScmFeedbackAdapter());
        client.tag(scmContext, new Revision(REVISION_DEFAULT_PREVIOUS), TAG_NAME, false);
        client.tag(scmContext, new Revision(REVISION_DEFAULT_LATEST), TAG_NAME, true);

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
        assertMercurialDir(baseDir);
    }

    private void assertFileContent(String expected, String path) throws IOException
    {
        File file = new File(baseDir, path);
        assertTrue(file.isFile());
        String content = Files.toString(file, Charset.defaultCharset());
        assertEquals(FileSystemUtils.normaliseNewlines(expected), FileSystemUtils.normaliseNewlines(content));
    }

    private void assertMercurialDir(File workingDir)
    {
        assertTrue(new File(workingDir, ".hg").isDirectory());
    }
}
