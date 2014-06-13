package com.zutubi.pulse.core.scm.git;

import com.google.common.base.Predicate;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.git.config.GitConfiguration;
import com.zutubi.util.Sort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Iterables.find;
import static com.zutubi.pulse.core.scm.git.GitConstants.*;
import static com.zutubi.pulse.core.test.api.Matchers.matchesRegex;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GitClientTest extends GitClientTestBase
{
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
        client.storeConnectionDetails(new PulseExecutionContext(), workingDir);
    }
    
    public void testGetEOLStyle() throws ScmException
    {
        assertEquals(EOLStyle.BINARY, client.getEOLPolicy(context));
    }

    public void testGetProperties() throws ScmException
    {
        List<ResourceProperty> properties = client.getProperties(new PulseExecutionContext());
        assertEquals(2, properties.size());
        ResourceProperty repositoryProperty = find(properties, new Predicate<ResourceProperty>()
        {
            public boolean apply(ResourceProperty resourceProperty)
            {
                return resourceProperty.getName().equals("git.repository");
            }
        }, null);
        assertNotNull(repositoryProperty);
        assertEquals(repository, repositoryProperty.getValue());
    }

    public void testCheckout() throws ScmException, ParseException
    {
        Revision rev = client.checkout(context, null, handler);

        assertEquals(REVISION_MASTER_LATEST, rev.getRevisionString());

        assertLatestCheckedOut();

        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testCheckoutOnBranch() throws ScmException, ParseException
    {
        checkoutBranchHelper();
    }

    public void testCheckoutSelectedBranch() throws ScmException, ParseException
    {
        client.setCloneType(GitConfiguration.CloneType.SELECTED_BRANCH_ONLY);
        checkoutBranchHelper();
    }

    public void testCheckoutFullMirror() throws ScmException, ParseException
    {
        client.setCloneType(GitConfiguration.CloneType.FULL_MIRROR);
        Revision rev = client.checkout(context, null, handler);

        assertEquals(REVISION_MASTER_LATEST, rev.getRevisionString());

        assertLatestCheckedOut();

        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    private void checkoutBranchHelper() throws ScmException
    {
        client.setBranch(BRANCH_SIMPLE);
        Revision rev = client.checkout(context, null, handler);

        assertEquals(REVISION_SIMPLE_LATEST, rev.getRevisionString());

        assertFiles(workingDir, "1.txt", "2.txt", "3.txt");
        assertGitDir(workingDir);
    }

    public void testCheckoutToRevision() throws ScmException, ParseException
    {
        Revision rev = client.checkout(context, new Revision(REVISION_INITIAL), handler);

        assertEquals(REVISION_INITIAL, rev.getRevisionString());

        assertFiles(workingDir, "a.txt", "b.txt", "c.txt");
        assertGitDir(workingDir);

        assertThat(handler.getStatusMessages().size(), greaterThan(0));
    }

    public void testCheckoutToRevisionOnBranch() throws ScmException, ParseException
    {
        checkoutBranchToRevisionHelper();
    }

    public void testCheckoutToRevisionSelectedBranch() throws ScmException, ParseException
    {
        client.setCloneType(GitConfiguration.CloneType.SELECTED_BRANCH_ONLY);
        checkoutBranchToRevisionHelper();
    }

    private void checkoutBranchToRevisionHelper() throws ScmException
    {
        client.setBranch(BRANCH_SIMPLE);
        Revision rev = client.checkout(context, new Revision(REVISION_SIMPLE_INTERMEDIATE), handler);

        assertEquals(REVISION_SIMPLE_INTERMEDIATE, rev.getRevisionString());

        assertGitDir(workingDir);
        assertEquals(1, workingDir.list().length);
    }

    public void testGetLatestRevision() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        Revision rev = client.getLatestRevision(scmContext);

        assertEquals(REVISION_MASTER_LATEST, rev.getRevisionString());
    }

    public void testGetLatestRevisionLatestFiltered() throws ScmException
    {
        // CIB-2918: Git retrieval of latest revision fails if last commit is filtered out by exclusions.
        client.setFilterPaths(Collections.<String>emptyList(), asList("a.txt", "c.txt", "d.txt"));
        client.init(scmContext, new ScmFeedbackAdapter());
        Revision rev = client.getLatestRevision(scmContext);

        assertEquals(REVISION_MASTER_PREVIOUS, rev.getRevisionString());
    }

    public void testGetLatestRevisionOnBranch() throws ScmException
    {
        client.setBranch(BRANCH_SIMPLE);
        client.init(scmContext, new ScmFeedbackAdapter());
        Revision rev = client.getLatestRevision(scmContext);

        assertEquals(REVISION_SIMPLE_LATEST, rev.getRevisionString());
    }

    public void testGetRevisions() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(asList(new Revision(REVISION_MASTER_PREVIOUS), new Revision(REVISION_MASTER_LATEST)), client.getRevisions(scmContext, new Revision(REVISION_MASTER_TWO_PREVIOUS), null));
    }

    public void testGetRevisionsInRange() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(
                asList(new Revision(REVISION_MASTER_PREVIOUS), new Revision(REVISION_MASTER_LATEST)),
                client.getRevisions(scmContext, new Revision(REVISION_MASTER_TWO_PREVIOUS), new Revision(REVISION_MASTER_LATEST))
        );
    }

    public void testGetRevisionsInReverseRange() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(
                asList(new Revision(REVISION_MASTER_LATEST), new Revision(REVISION_MASTER_PREVIOUS)),
                client.getRevisions(scmContext, new Revision(REVISION_MASTER_LATEST), new Revision(REVISION_MASTER_TWO_PREVIOUS))
        );
    }

    public void testGetRevisionsAfterUpstreamCommit() throws ScmException, IOException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(REVISION_MASTER_LATEST, client.getLatestRevision(scmContext).getRevisionString());

        // Making a commit after we init ensures a working fetch/merge for this
        // expected fast-forward case.
        makeUpstreamCommit();

        assertEquals(1, client.getRevisions(scmContext, new Revision(REVISION_MASTER_LATEST), null).size());
    }

    public void testGetRevisionsAfterRewritingHistory() throws ScmException, IOException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(REVISION_MASTER_LATEST, client.getLatestRevision(scmContext).getRevisionString());

        rewriteUpstreamHistory();

        try
        {
            client.getRevisions(scmContext, new Revision(REVISION_MASTER_LATEST), null);
            fail("Shouldn't be able to get revisions when pull would require merge.");
        }
        catch (ScmException e)
        {
            assertEquals("Fetch is not fast-forward, likely due to history changes upstream.  Project reinitialisation required.", e.getMessage());
        }
    }

    public void testGetRevisionsOnNewBranch() throws ScmException, IOException
    {
        final String BRANCH_NEW = "newbranch";
        
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(REVISION_MASTER_LATEST, client.getLatestRevision(scmContext).getRevisionString());

        NativeGit git = getNativeGitOnMaster();
        git.run(git.getGitCommand(), "branch", BRANCH_NEW);
        client.setBranch(BRANCH_NEW);

        assertEquals(1, client.getRevisions(scmContext, new Revision(REVISION_MASTER_PREVIOUS), null).size());
    }

    private void makeUpstreamCommit() throws IOException, ScmException
    {
        NativeGit git = getNativeGitOnMaster();

        File b = new File(repositoryBase, "b.txt");
        Files.write(getName(), b, Charset.defaultCharset());
        git.run(git.getGitCommand(), COMMAND_COMMIT, FLAG_ADD, FLAG_MESSAGE, "Will need merging");
    }

    private void rewriteUpstreamHistory() throws ScmException, IOException
    {
        // Reset to delete the latest master commit
        NativeGit git = getNativeGitOnMaster();
        git.run(git.getGitCommand(), COMMAND_RESET, FLAG_HARD, "HEAD~1");
        makeUpstreamCommit();
    }

    private NativeGit getNativeGitOnMaster() throws ScmException
    {
        // Add a new commit which requires merging
        NativeGit git = new NativeGit(0, null);
        git.setWorkingDirectory(repositoryBase);
        git.checkout(null, "master");
        return git;
    }

    public void testRetrieve() throws ScmException, IOException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals(CONTENT_A_TXT, ScmUtils.retrieveContent(client, scmContext, "a.txt", null));
    }

    public void testRetrieveFromRevision() throws IOException, ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals("content", ScmUtils.retrieveContent(client, scmContext, "a.txt", new Revision(REVISION_MASTER_INTERMEDIATE)));
    }

    public void testRetrieveOnBranch() throws ScmException, IOException
    {
        client.setBranch(BRANCH_SIMPLE);
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals("", ScmUtils.retrieveContent(client, scmContext, "1.txt", null));
    }

    public void testRetrieveFromRevisionOnBranch() throws ScmException, IOException
    {
        client.setBranch(BRANCH_SIMPLE);
        client.init(scmContext, new ScmFeedbackAdapter());
        assertEquals("content", ScmUtils.retrieveContent(client, scmContext, "1.txt", new Revision("7d61890eb55586ec99416c53c581bf561591a608")));
    }

    public void testUpdate() throws ScmException
    {
        client.checkout(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertThat(handler.getStatusMessages(), hasItem(startsWith("Branch local set up")));
        assertThat(handler.getStatusMessages(), not(hasItem(GitClient.I18N.format(GitClient.KEY_INCOMPLETE_CHECKOUT))));

        handler.reset();

        client.update(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals("Already up-to-date.", handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
    }

    public void testUpdateOnBranch() throws ScmException
    {
        updateOnBranchHelper();
    }

    public void testUpdateSelectedBranch() throws ScmException
    {
        client.setCloneType(GitConfiguration.CloneType.SELECTED_BRANCH_ONLY);
        updateOnBranchHelper();
    }

    private void updateOnBranchHelper() throws ScmException
    {
        client.setBranch(BRANCH_SIMPLE);
        client.checkout(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertThat(handler.getStatusMessages(), anyOf(hasItem(startsWith("Branch local set up")), hasItem(startsWith("Switched to a new branch 'local'"))));

        handler.reset();

        client.update(context, null, handler);
        assertThat(handler.getStatusMessages().size(), greaterThan(0));
        assertEquals("Already up-to-date.", handler.getStatusMessages().get(handler.getStatusMessages().size() - 1));
    }

    public void testUpdateToLatestOnBranch() throws ScmException, IOException, ParseException
    {
        updateToLatestOnBranchHelper();
    }

    public void testUpdateToLatestSelectedBranch() throws ScmException, IOException, ParseException
    {
        client.setCloneType(GitConfiguration.CloneType.SELECTED_BRANCH_ONLY);
        updateToLatestOnBranchHelper();
    }

    private void updateToLatestOnBranchHelper() throws ScmException, IOException
    {
        client.setBranch(BRANCH_SIMPLE);
        client.checkout(context, new Revision(REVISION_SIMPLE_INTERMEDIATE), handler);
        assertEquals(1, workingDir.list().length);

        Revision rev = client.update(context, null, handler);
        assertEquals(REVISION_SIMPLE_LATEST, rev.getRevisionString());

        assertFiles(workingDir, "1.txt", "2.txt", "3.txt");
        assertGitDir(workingDir);
    }

    public void testUpdateToRevision() throws ScmException, IOException, ParseException
    {
        updateToRevisionHelper();
    }

    public void testUpdateToRevisionSelectedBranch() throws ScmException, IOException, ParseException
    {
        client.setCloneType(GitConfiguration.CloneType.SELECTED_BRANCH_ONLY);
        updateToRevisionHelper();
    }

    private void updateToRevisionHelper() throws ScmException, IOException
    {
        client.checkout(context, null, handler);
        assertEquals(CONTENT_A_TXT, Files.toString(new File(workingDir, "a.txt"), Charset.defaultCharset()));

        Revision rev = client.update(context, new Revision(REVISION_MASTER_INTERMEDIATE), handler);
        assertEquals(REVISION_MASTER_INTERMEDIATE, rev.getRevisionString());

        assertEquals("content", Files.toString(new File(workingDir, "a.txt"), Charset.defaultCharset()));
    }

    public void testUpdateNoCheckout() throws ScmException
    {
        client.update(context, null, handler);
        assertThat(handler.getStatusMessages(), hasItem(GitClient.I18N.format(GitClient.KEY_INCOMPLETE_CHECKOUT)));
        assertLatestCheckedOut();
    }

    public void testUpdateIncompleteCheckout() throws ScmException
    {
        client.checkout(context, null, handler);
        handler.reset();

        File markerFile = client.getMarkerFile(workingDir);
        assertTrue(markerFile.delete());

        client.update(context, null, handler);
        assertThat(handler.getStatusMessages(), hasItem(GitClient.I18N.format(GitClient.KEY_INCOMPLETE_CHECKOUT)));
        assertLatestCheckedOut();
    }

    public void testCheckoutOldMarkerRecognised() throws ScmException, IOException
    {
        client.checkout(context, null, handler);
        handler.reset();

        File markerFile = client.getMarkerFile(workingDir);
        File oldMarkerFile = new File(workingDir, GitClient.CHECKOUT_COMPLETE_FILENAME);
        assertTrue(markerFile.renameTo(oldMarkerFile));

        client.update(context, null, handler);
        assertThat(handler.getStatusMessages(), not(hasItem(GitClient.I18N.format(GitClient.KEY_INCOMPLETE_CHECKOUT))));
        assertLatestCheckedOut();
    }

    public void testUpdateAfterRewritingHistory() throws ScmException, IOException
    {
        client.checkout(context, null, handler);
        handler.reset();

        rewriteUpstreamHistory();

        client.update(context, null, handler);
        assertThat(handler.getStatusMessages(), hasItem(GitClient.I18N.format(GitClient.KEY_MERGE_ON_UPDATE)));
        assertFiles(workingDir, "a.txt", "b.txt", "c.txt");
    }

    public void testChanges() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        List<Changelist> changes = client.getChanges(scmContext, new Revision(REVISION_MASTER_LATEST + "~2"), null);
        assertEquals(2, changes.size());
        assertEquals("b.txt", changes.get(0).getChanges().get(0).getPath());
        assertEquals("a.txt", changes.get(1).getChanges().get(0).getPath());
    }

    public void testChangesReverseRange() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        List<Changelist> changes = client.getChanges(scmContext, new Revision(REVISION_MASTER_LATEST), new Revision(REVISION_MASTER_LATEST + "~2"));
        assertEquals(2, changes.size());
        assertEquals("a.txt", changes.get(0).getChanges().get(0).getPath());
        assertEquals("b.txt", changes.get(1).getChanges().get(0).getPath());
    }

    public void testLatestChanges() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());

        List<Changelist> changes = client.getChanges(scmContext, new Revision(REVISION_MASTER_LATEST + "~1"), new Revision(REVISION_MASTER_LATEST));
        assertEquals(1, changes.size());
        Changelist changelist = changes.get(0);
        assertEquals("Edit, add and remove on master.", changelist.getComment());
        assertEquals(REVISION_MASTER_LATEST, changelist.getRevision().getRevisionString());
        assertEquals(TEST_AUTHOR, changelist.getAuthor());
        List<FileChange> fileChanges = changelist.getChanges();
        assertEquals(3, fileChanges.size());

        FileChange change = fileChanges.get(0);
        assertEquals(FileChange.Action.EDIT, change.getAction());
        assertEquals("a.txt", change.getPath());

        change = fileChanges.get(1);
        assertEquals(FileChange.Action.DELETE, change.getAction());
        assertEquals("c.txt", change.getPath());

        change = fileChanges.get(2);
        assertEquals(FileChange.Action.ADD, change.getAction());
        assertEquals("d.txt", change.getPath());
    }

    public void testLatestChangesWithExcludes() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        client.setFilterPaths(Collections.<String>emptyList(), Arrays.asList("**/*"));

        List<Changelist> changes = client.getChanges(scmContext, new Revision(REVISION_MASTER_LATEST + "~1"), new Revision(REVISION_MASTER_LATEST));
        assertEquals(0, changes.size());
    }

    public void testLatestChangesWithSpecificExcludes() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        client.setFilterPaths(Collections.<String>emptyList(), Arrays.asList("**/c.txt"));

        List<Changelist> changes = client.getChanges(scmContext, new Revision(REVISION_MASTER_LATEST + "~1"), new Revision(REVISION_MASTER_LATEST));
        assertEquals(1, changes.size());
        Changelist changelist = changes.get(0);
        List<FileChange> fileChanges = changelist.getChanges();
        assertEquals(2, fileChanges.size());
    }

    public void testMergedChange() throws ScmException
    {
        client.setBranch(BRANCH_MERGES);
        client.init(scmContext, new ScmFeedbackAdapter());

        // Log over the whole branch history as the interesting cases happen
        // when there are multiple log entries in a single output stream that
        // we need to pull apart.
        List<Changelist> changes = client.getChanges(scmContext, new Revision(REVISION_INITIAL), new Revision(REVISION_DEV_MERGE_CONFLICTS));

        Changelist changelist = assertChangelist(changes, REVISION_DEV_MERGE_NO_CONFLICTS);
        assertEquals("Merge branch 'master' into devbranch", changelist.getComment());
        assertEquals(TEST_AUTHOR, changelist.getAuthor());
        assertEquals(0, changelist.getChanges().size());

        changelist = assertChangelist(changes, REVISION_DEV_MERGE_CONFLICTS);
        assertEquals("Fixed merge conflict.", changelist.getComment());
        assertEquals(TEST_AUTHOR, changelist.getAuthor());
        assertEquals(1, changelist.getChanges().size());
        
        FileChange change = changelist.getChanges().get(0);
        assertEquals("a.txt", change.getPath());
        assertEquals(FileChange.Action.MERGE, change.getAction());
    }

    public void testChangeWithMultilineComment() throws ScmException
    {
        client.setBranch(BRANCH_MERGES);
        client.init(scmContext, new ScmFeedbackAdapter());

        List<Changelist> changes = client.getChanges(scmContext, new Revision(REVISION_INITIAL), new Revision(REVISION_MULTILINE_COMMENT));

        Changelist changelist = assertChangelist(changes, REVISION_MULTILINE_COMMENT);
        // Some versions of git are more aggressive at collapsing the "subject"
        // or initial part of a commit comment than others, so be a bit
        // permissive.  Just ensure that the whole comment makes it, with some
        // kind of separating whitespace.
        assertThat(changelist.getComment(), matchesRegex("This is a\\smulti-line\\scommit comment"));
        assertEquals(TEST_AUTHOR, changelist.getAuthor());
        assertEquals(1, changelist.getChanges().size());

        FileChange change = changelist.getChanges().get(0);
        assertEquals("a.txt", change.getPath());
        assertEquals(FileChange.Action.EDIT, change.getAction());
    }

    private Changelist assertChangelist(List<Changelist> changes, String revision)
    {
        Changelist changelist = findChangelist(changes, revision);
        assertNotNull(changelist);
        return changelist;
    }

    private Changelist findChangelist(List<Changelist> changes, final String revision)
    {
        return find(changes, new Predicate<Changelist>()
        {
            public boolean apply(Changelist changelist)
            {
                return changelist.getRevision().getRevisionString().equals(revision);
            }
        }, null);
    }

    public void testBrowse() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());

        List<ScmFile> files = client.browse(scmContext, "", null);
        assertEquals(3, files.size());
        Collections.sort(files, new ScmFileComparator());
        assertEquals("a.txt", files.get(0).getName());
        assertEquals("b.txt", files.get(1).getName());
        assertEquals("d.txt", files.get(2).getName());
    }

    public void testBrowseAtRevision() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());

        List<ScmFile> files = client.browse(scmContext, "", new Revision(REVISION_INITIAL));
        assertEquals(3, files.size());
        Collections.sort(files, new ScmFileComparator());
        assertEquals("a.txt", files.get(0).getName());
        assertEquals("b.txt", files.get(1).getName());
        assertEquals("c.txt", files.get(2).getName());
    }

    public void testBrowseFile() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());

        List<ScmFile> files = client.browse(scmContext, "a.txt", null);
        assertEquals(1, files.size());
        assertEquals("a.txt", files.get(0).getName());
    }

    public void testBrowseNotAvailableWhenContextNotAvailable()
    {
        assertFalse(client.getCapabilities(new ScmContextImpl(null, null)).contains(ScmCapability.BROWSE));
    }

    public void testBrowseAvailableWhenContextAvailable()
    {
        assertTrue(client.getCapabilities(scmContext).contains(ScmCapability.BROWSE));
    }

    public void testTestConnectionOK() throws ScmException
    {
        client.testConnection(scmContext);
    }

    public void testTestConnectionBadRepo()
    {
        client = new GitClient("file:///no/such/repo", "master", 0, GitConfiguration.CloneType.NORMAL, 0, false, Collections.<String>emptyList());
        try
        {
            client.testConnection(scmContext);
            fail("Test of bad repo should fail");
        }
        catch (ScmException e)
        {
            assertThat(e.getMessage(), containsString("ls-remote file:///no/such/repo master' exited with non-zero exit code"));
        }
    }

    public void testTestConnectionBadBranch()
    {
        client = new GitClient(repository, "nosuchbranch", 0, GitConfiguration.CloneType.NORMAL, 0, false, Collections.<String>emptyList());
        try
        {
            client.testConnection(scmContext);
            fail("Test of bad branch should fail");
        }
        catch (ScmException e)
        {
            assertThat(e.getMessage(), containsString("Branch 'nosuchbranch' does not exist"));
        }
    }

    public void testTagRevision() throws ScmException, IOException
    {
        final String TAG_NAME = "test-tag";

        client.init(scmContext, new ScmFeedbackAdapter());
        client.tag(scmContext, new Revision(REVISION_INITIAL), TAG_NAME, false);

        final NativeGit nativeGit = new NativeGit();
        nativeGit.setWorkingDirectory(repositoryBase);
        String info = showTag(nativeGit, TAG_NAME);
        assertThat(info, containsString(REVISION_INITIAL));
        assertThat(info, containsString("initial commit"));
    }

    public void testGetEmailLatest() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        String email = client.getEmailAddress(scmContext, "Jason Sankey");
        assertEquals("jason@zutubi.com", email);
    }

    public void testGetEmailHistorical() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        String email = client.getEmailAddress(scmContext, "Daniel Ostermeier");
        assertEquals("daniel@zutubi.com", email);
    }

    public void testGetEmailUnrecognisedUser() throws ScmException
    {
        client.init(scmContext, new ScmFeedbackAdapter());
        String email = client.getEmailAddress(scmContext, "Who IsThis");
        assertNull(email);
    }

    private String showTag(final NativeGit nativeGit, final String tagName) throws IOException
    {
        return CharStreams.toString(CharStreams.newReaderSupplier(new InputSupplier<InputStream>()
        {
            public InputStream getInput() throws IOException
            {
                try
                {
                    return nativeGit.show(null, tagName);
                }
                catch (ScmException e)
                {
                    throw new IOException(e);
                }
            }
        }, Charset.defaultCharset()));
    }

    private void assertLatestCheckedOut()
    {
        assertFiles(workingDir, "a.txt", "b.txt", "d.txt");
        assertGitDir(workingDir);
    }

    private void assertFiles(File base, String... filenames)
    {
        for (String filename : filenames)
        {
            assertTrue(filename + " is not a file.", new File(base, filename).isFile());
        }
    }

    private void assertGitDir(File workingDir)
    {
        assertTrue(new File(workingDir, GitClient.GIT_REPOSITORY_DIRECTORY).isDirectory());
    }

    private static class ScmFileComparator implements Comparator<ScmFile>
    {
        private final Sort.StringComparator comp = new Sort.StringComparator();

        public int compare(ScmFile o1, ScmFile o2)
        {
            return comp.compare(o1.getName(), o2.getName());
        }
    }
}
